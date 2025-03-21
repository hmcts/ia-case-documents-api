package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.Appender;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.EmBundleRequestExecutor;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.Bundle;

@Component
public class CustomiseHearingBundleHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private static final String SUPPLIED_BY_RESPONDENT = "The respondent";
    private static final String SUPPLIED_BY_APPELLANT = "The appellant";
    private static final String MISSING_DOCUMENT_EXCEPTION_MESSAGE = "Document cannot be null";

    private final EmBundleRequestExecutor emBundleRequestExecutor;
    private final Appender<DocumentWithMetadata> documentWithMetadataAppender;
    private final DateProvider dateProvider;
    private final String emBundlerUrl;
    private final String emBundlerStitchUri;
    private final ObjectMapper objectMapper;
    private final FeatureToggler featureToggler;

    public CustomiseHearingBundleHandler(
        @Value("${emBundler.url}") String emBundlerUrl,
        @Value("${emBundler.stitch.async.uri}") String emBundlerStitchUri,
        EmBundleRequestExecutor emBundleRequestExecutor,
        Appender<DocumentWithMetadata> documentWithMetadataAppender,
        DateProvider dateProvider,
        ObjectMapper objectMapper,
        FeatureToggler featureToggler
    ) {
        this.emBundlerUrl = emBundlerUrl;
        this.emBundlerStitchUri = emBundlerStitchUri;
        this.emBundleRequestExecutor = emBundleRequestExecutor;
        this.documentWithMetadataAppender = documentWithMetadataAppender;
        this.dateProvider = dateProvider;
        this.objectMapper = objectMapper;
        this.featureToggler = featureToggler;

    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && (callback.getEvent() == Event.CUSTOMISE_HEARING_BUNDLE
            || callback.getEvent() == Event.GENERATE_UPDATED_HEARING_BUNDLE);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();
        asylumCase.clear(AsylumCaseDefinition.HMCTS);
        asylumCase.write(AsylumCaseDefinition.HMCTS, "[userImage:hmcts.png]");
        asylumCase.clear(AsylumCaseDefinition.CASE_BUNDLES);


        boolean isReheardCase = asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false)
            && featureToggler.getValue("reheard-feature", false);
        boolean isRemittedPath = asylumCase.read(SOURCE_OF_REMITTAL, String.class).isPresent();

        boolean isOrWasAda = asylumCase.read(SUITABILITY_REVIEW_DECISION).isPresent();
        boolean isUpdatedBundle = callback.getEvent().equals(Event.GENERATE_UPDATED_HEARING_BUNDLE);
        if (isReheardCase || isUpdatedBundle) {
            //populate these collections to avoid error on the Stitching api
            initializeNewCollections(asylumCase);
        }
        String bundle;
        if (isReheardCase) {
            bundle = isRemittedPath ? "iac-remitted-reheard-hearing-bundle-config.yaml" : "iac-reheard-hearing-bundle-config.yaml";
        } else if (isUpdatedBundle) {
            bundle = isOrWasAda ? "iac-updated-hearing-bundle-inc-tribunal-config.yaml" : "iac-updated-hearing-bundle-config.yaml";
        } else {
            bundle = isOrWasAda ? "iac-hearing-bundle-inc-tribunal-config.yaml" : "iac-hearing-bundle-config.yaml";
        }

        asylumCase.write(AsylumCaseDefinition.BUNDLE_CONFIGURATION, bundle);
        asylumCase.write(AsylumCaseDefinition.BUNDLE_FILE_NAME_PREFIX, getBundlePrefix(asylumCase));

        //deep copy the case
        AsylumCase asylumCaseCopy;
        try {
            asylumCaseCopy = objectMapper
                .readValue(objectMapper.writeValueAsString(asylumCase), AsylumCase.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot make a deep copy of the case");
        }

        prepareDocuments(getMappingFields(isReheardCase, isRemittedPath, isOrWasAda, isUpdatedBundle), asylumCaseCopy);
        if (isReheardCase) {
            prepareDocuments(getMappingFieldsForAdditionalEvidenceDocuments(), asylumCaseCopy);
        }

        final PreSubmitCallbackResponse<AsylumCase> response = emBundleRequestExecutor.post(
            new Callback<>(
                new CaseDetails<>(
                    callback.getCaseDetails().getId(),
                    callback.getCaseDetails().getJurisdiction(),
                    callback.getCaseDetails().getState(),
                    asylumCaseCopy,
                    callback.getCaseDetails().getCreatedDate()
                ),
                callback.getCaseDetailsBefore(),
                callback.getEvent()
            ),
            emBundlerUrl + emBundlerStitchUri);

        final AsylumCase responseData = response.getData();

        restoreCollections(asylumCase, asylumCaseCopy, isReheardCase);

        restoreAddendumEvidence(asylumCase, asylumCaseCopy, isReheardCase, isUpdatedBundle);

        restoreRemittalDocumentsInCollections(asylumCase, asylumCaseCopy, isRemittedPath);
        // for cases which progressed to finalBundling pre set-aside release,
        // we want that all the documents should be restored in the collection field
        restoreReheardDocumentsInCollections(asylumCase, asylumCaseCopy, isRemittedPath
            ? LATEST_REHEARD_HEARING_DOCUMENTS : REHEARD_HEARING_DOCUMENTS, REHEARD_HEARING_DOCUMENTS_COLLECTION);
        restoreReheardDocumentsInCollections(asylumCase, asylumCaseCopy,
            LATEST_DECISION_AND_REASONS_DOCUMENTS, REHEARD_DECISION_REASONS_COLLECTION);

        Optional<List<IdValue<Bundle>>> maybeCaseBundles = responseData.read(AsylumCaseDefinition.CASE_BUNDLES);
        asylumCase.write(AsylumCaseDefinition.CASE_BUNDLES, maybeCaseBundles);

        final List<Bundle> caseBundles = maybeCaseBundles
            .orElseThrow(() -> new IllegalStateException("caseBundle is not present"))
            .stream()
            .map(IdValue::getValue)
            .toList();

        if (caseBundles.size() != 1) {
            throw new IllegalStateException("case bundles size is not 1 and is : " + caseBundles.size());
        }

        //stitchStatusflags -  NEW, IN_PROGRESS, DONE, FAILED
        final String stitchStatus = caseBundles.get(0).getStitchStatus().orElse("");

        asylumCase.write(AsylumCaseDefinition.STITCHING_STATUS, stitchStatus);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    void initializeNewCollections(AsylumCase asylumCase) {
        if (asylumCase.read(APPELLANT_ADDENDUM_EVIDENCE_DOCS).isEmpty()) {
            asylumCase.write(APPELLANT_ADDENDUM_EVIDENCE_DOCS, emptyList());
        }

        if (asylumCase.read(RESPONDENT_ADDENDUM_EVIDENCE_DOCS).isEmpty()) {
            asylumCase.write(RESPONDENT_ADDENDUM_EVIDENCE_DOCS, emptyList());
        }

        if (asylumCase.read(APP_ADDITIONAL_EVIDENCE_DOCS).isEmpty()) {
            asylumCase.write(APP_ADDITIONAL_EVIDENCE_DOCS, emptyList());
        }

        if (asylumCase.read(RESP_ADDITIONAL_EVIDENCE_DOCS).isEmpty()) {
            asylumCase.write(RESP_ADDITIONAL_EVIDENCE_DOCS, emptyList());
        }

        if (asylumCase.read(LATEST_REMITTAL_DOCUMENTS).isEmpty()) {
            asylumCase.write(LATEST_REMITTAL_DOCUMENTS, emptyList());
        }

        if (asylumCase.read(LATEST_REHEARD_HEARING_DOCUMENTS).isEmpty()) {
            asylumCase.write(LATEST_REHEARD_HEARING_DOCUMENTS, emptyList());
        }

        if (asylumCase.read(LATEST_DECISION_AND_REASONS_DOCUMENTS).isEmpty()) {
            asylumCase.write(LATEST_DECISION_AND_REASONS_DOCUMENTS, emptyList());
        }
    }

    Optional<IdValue<DocumentWithMetadata>> isDocumentWithDescriptionPresent(
        List<IdValue<DocumentWithMetadata>> idValueList,
        IdValue<DocumentWithDescription> documentWithDescription
    ) {

        IdValue<DocumentWithMetadata> documentWithMetadataIdValue = null;

        for (IdValue<DocumentWithMetadata> doc : idValueList) {
            Document legalDocument = doc.getValue().getDocument();
            Document document = documentWithDescription.getValue().getDocument().orElseThrow(() -> new IllegalStateException(MISSING_DOCUMENT_EXCEPTION_MESSAGE));
            if (legalDocument.getDocumentBinaryUrl().equals(document.getDocumentBinaryUrl())) {
                documentWithMetadataIdValue = doc;
            }
        }

        return Optional.ofNullable(documentWithMetadataIdValue);
    }

    private void restoreAddendumEvidence(AsylumCase asylumCase, AsylumCase asylumCaseBefore, boolean isReheardCase, boolean isUpdatedBundle) {
        if (!isReheardCase && !isUpdatedBundle) {
            return;
        }

        Optional<List<IdValue<DocumentWithMetadata>>> currentAppellantAddendumEvidenceDocs = asylumCaseBefore.read(APPELLANT_ADDENDUM_EVIDENCE_DOCS);
        Optional<List<IdValue<DocumentWithMetadata>>> currentRespondentAddendumEvidenceDocs = asylumCaseBefore.read(RESPONDENT_ADDENDUM_EVIDENCE_DOCS);

        Optional<List<IdValue<DocumentWithMetadata>>> maybeAddendumEvidenceDocs = asylumCaseBefore.read(ADDENDUM_EVIDENCE_DOCUMENTS);

        List<IdValue<DocumentWithMetadata>> beforeDocuments = new ArrayList<>();

        if (maybeAddendumEvidenceDocs.isPresent()) {
            beforeDocuments = getIdValuesBefore(asylumCaseBefore, ADDENDUM_EVIDENCE_DOCUMENTS);
        }
        //filter any document missing from the current list of document
        List<IdValue<DocumentWithMetadata>> missingAppellantDocuments = beforeDocuments
            .stream()
            .filter(document -> document.getValue().getSuppliedBy().equals(SUPPLIED_BY_APPELLANT))
            .filter(document -> !contains(currentAppellantAddendumEvidenceDocs.orElse(emptyList()), document))
            .toList();

        List<IdValue<DocumentWithMetadata>> allAppellantDocuments = currentAppellantAddendumEvidenceDocs.orElse(emptyList());

        for (IdValue<DocumentWithMetadata> documentWithMetadata : missingAppellantDocuments) {
            allAppellantDocuments = documentWithMetadataAppender.append(documentWithMetadata.getValue(), allAppellantDocuments);
        }

        List<IdValue<DocumentWithMetadata>> missingRespondentDocuments = beforeDocuments
            .stream()
            .filter(document -> document.getValue().getSuppliedBy().equals(SUPPLIED_BY_RESPONDENT))
            .filter(document -> !contains(currentRespondentAddendumEvidenceDocs.orElse(emptyList()), document))
            .toList();

        List<IdValue<DocumentWithMetadata>> allRespondentDocuments = currentRespondentAddendumEvidenceDocs.orElse(emptyList());
        for (IdValue<DocumentWithMetadata> documentWithMetadata : missingRespondentDocuments) {
            allRespondentDocuments = documentWithMetadataAppender.append(documentWithMetadata.getValue(), allRespondentDocuments);
        }

        //add the 2 list
        List<IdValue<DocumentWithMetadata>> allDocuments = new ArrayList<>();

        for (IdValue<DocumentWithMetadata> documentWithMetadata : allAppellantDocuments) {
            allDocuments = documentWithMetadataAppender.append(documentWithMetadata.getValue(), allDocuments);
        }

        for (IdValue<DocumentWithMetadata> documentWithMetadata : allRespondentDocuments) {
            allDocuments = documentWithMetadataAppender.append(documentWithMetadata.getValue(), allDocuments);
        }

        asylumCase.clear(ADDENDUM_EVIDENCE_DOCUMENTS);
        asylumCase.write(ADDENDUM_EVIDENCE_DOCUMENTS, allDocuments);

    }


    private void restoreCollections(
        AsylumCase asylumCase,
        AsylumCase asylumCaseBefore,
        boolean isReheardCase
    ) {
        boolean isOrWasAda = asylumCase.read(SUITABILITY_REVIEW_DECISION).isPresent();
        getFieldDefinitions(isReheardCase, isOrWasAda).forEach(field -> {
            Optional<List<IdValue<DocumentWithMetadata>>> currentIdValues = asylumCase.read(field);
            Optional<List<IdValue<DocumentWithMetadata>>> beforeIdValues = asylumCaseBefore.read(field);

            List<IdValue<DocumentWithMetadata>> beforeDocuments = new ArrayList<>();

            if (beforeIdValues.isPresent()) {
                beforeDocuments = getIdValuesBefore(asylumCaseBefore, field);
            }
            //filter any document missing from the current list of document
            List<IdValue<DocumentWithMetadata>> missingDocuments = beforeDocuments
                .stream()
                .filter(document -> !contains(currentIdValues.orElse(emptyList()), document))
                .toList();

            List<IdValue<DocumentWithMetadata>> allDocuments = currentIdValues.orElse(emptyList());
            for (IdValue<DocumentWithMetadata> documentWithMetadata : missingDocuments) {
                allDocuments = documentWithMetadataAppender.append(documentWithMetadata.getValue(), allDocuments);
            }

            asylumCase.clear(field);
            asylumCase.write(field, allDocuments);

        });
    }

    private void restoreReheardDocumentsInCollections(AsylumCase asylumCase, AsylumCase asylumCaseBefore, AsylumCaseDefinition latestField, AsylumCaseDefinition existingField) {
        // Retrieve the current reheard hearing documents from the latest field in the current asylum case
        Optional<List<IdValue<DocumentWithMetadata>>> maybeCurrentReheardHearingDocs = asylumCaseBefore.read(latestField);
        List<IdValue<DocumentWithMetadata>> currentReheardHearingDocs = maybeCurrentReheardHearingDocs.orElse(emptyList());

        //Retrieve the existing reheard hearing documents from the existing field in the asylum case before changes
        Optional<List<IdValue<ReheardHearingDocuments>>> maybeExistingReheardHearingDocs = asylumCaseBefore.read(existingField);
        List<IdValue<ReheardHearingDocuments>> existingReheardDocs = maybeExistingReheardHearingDocs.orElse(emptyList());
        // Initialize variables to store documents from the asylum case before changes
        List<IdValue<DocumentWithMetadata>> beforeDocuments = new ArrayList<>();
        ReheardHearingDocuments beforeReheardDocs = new ReheardHearingDocuments();
        // If existing reheard hearing documents exist, extract the list of documents
        if (!existingReheardDocs.isEmpty()) {
            beforeReheardDocs = existingReheardDocs.get(0).getValue();
            beforeDocuments = beforeReheardDocs.getReheardHearingDocs();
        }
        currentReheardHearingDocs = restoreDocumentsInCollection(currentReheardHearingDocs, beforeDocuments);
        //Changed the documents in the first Reheard object
        beforeReheardDocs.setReheardHearingDocs(currentReheardHearingDocs);
        //Scenario : If there was no collection field to begin with, and now the documents should ultimately be restored in the collection field.
        if (existingReheardDocs.isEmpty() && !currentReheardHearingDocs.isEmpty()) {
            existingReheardDocs = List.of(new IdValue<>("1", beforeReheardDocs));
        }
        asylumCase.write(existingField, existingReheardDocs);
    }

    private void restoreRemittalDocumentsInCollections(AsylumCase asylumCase, AsylumCase asylumCaseBefore, boolean isRemittedFeature) {

        if (!isRemittedFeature) {
            return;
        }
        Optional<List<IdValue<DocumentWithMetadata>>> maybeCurrentRemittalDocs = asylumCaseBefore.read(LATEST_REMITTAL_DOCUMENTS);
        List<IdValue<DocumentWithMetadata>> currentRemittalDocuments = maybeCurrentRemittalDocs.orElse(emptyList());

        Optional<List<IdValue<RemittalDocument>>> maybeRemittalDocs = asylumCaseBefore.read(REMITTAL_DOCUMENTS);
        List<IdValue<RemittalDocument>> existingRemittalDocs = maybeRemittalDocs.orElse(emptyList());
        List<IdValue<DocumentWithMetadata>> beforeDocuments = new ArrayList<>();
        RemittalDocument beforeRemittalDocuments = new RemittalDocument();
        String idValue = "1";

        if (!existingRemittalDocs.isEmpty()) {
            idValue = existingRemittalDocs.get(0).getId();
            beforeRemittalDocuments = existingRemittalDocs.get(0).getValue();
            beforeDocuments = beforeRemittalDocuments.getOtherRemittalDocs();
        }
        currentRemittalDocuments = restoreDocumentsInCollection(currentRemittalDocuments, beforeDocuments);

        //Remove the remittal decision document (Identifying as the decision document is renamed while storing with certain keywords)
        List<IdValue<DocumentWithMetadata>> filteredList = currentRemittalDocuments.stream()
            .filter(document -> !document.getValue().getDocument().getDocumentFilename().contains("-Decision-to-remit.pdf"))
            .collect(Collectors.toList());

        //Changed the documents in the latest RemittalDocs object
        beforeRemittalDocuments.setOtherRemittalDocs(filteredList);
        existingRemittalDocs.set(0, new IdValue<>(idValue, beforeRemittalDocuments));
        asylumCase.write(REMITTAL_DOCUMENTS, existingRemittalDocs);
    }

    private List<IdValue<DocumentWithMetadata>> restoreDocumentsInCollection(List<IdValue<DocumentWithMetadata>> currentList, List<IdValue<DocumentWithMetadata>> existingList) {
        List<IdValue<DocumentWithMetadata>> finalCurrentList = currentList;
        List<IdValue<DocumentWithMetadata>> missingDocuments = existingList
            .stream()
            .filter(document -> !contains(finalCurrentList, document))
            .toList();

        for (IdValue<DocumentWithMetadata> documentWithMetadata : missingDocuments) {
            currentList = documentWithMetadataAppender.append(documentWithMetadata.getValue(), currentList);
        }
        restoreDocumentMetaData(currentList, existingList);
        return currentList;
    }

    boolean contains(
        List<IdValue<DocumentWithMetadata>> existingDocuments,
        IdValue<DocumentWithMetadata> documentWithMetadata
    ) {

        boolean found = false;

        for (IdValue<DocumentWithMetadata> doc : existingDocuments) {
            Document legalDocument = doc.getValue().getDocument();
            Document document = documentWithMetadata.getValue().getDocument();
            if (legalDocument.getDocumentBinaryUrl().equals(document.getDocumentBinaryUrl())) {
                found = true;
            }
        }

        return found;
    }

    private IdValue<DocumentWithMetadata> getExistingDocument(
        List<IdValue<DocumentWithMetadata>> existingDocuments,
        IdValue<DocumentWithMetadata> documentWithMetadata
    ) {
        for (IdValue<DocumentWithMetadata> doc : existingDocuments) {
            Document legalDocument = doc.getValue().getDocument();
            Document document = documentWithMetadata.getValue().getDocument();
            if (legalDocument.getDocumentBinaryUrl().equals(document.getDocumentBinaryUrl())) {
                return doc;
            }
        }
        return null;
    }

    private void restoreDocumentMetaData(List<IdValue<DocumentWithMetadata>> finalCurrentList, List<IdValue<DocumentWithMetadata>> existingList) {
        for (int i = 0; i < finalCurrentList.size(); i++) {
            IdValue<DocumentWithMetadata> currentDocument = finalCurrentList.get(i);

            if (contains(existingList, currentDocument)) {
                IdValue<DocumentWithMetadata> existingDocument = getExistingDocument(existingList, currentDocument);

                if (existingDocument != null) {
                    DocumentWithMetadata alteredDoc = new DocumentWithMetadata(
                        currentDocument.getValue().getDocument(),
                        currentDocument.getValue().getDescription(),
                        existingDocument.getValue().getDateUploaded(),
                        existingDocument.getValue().getTag(),
                        existingDocument.getValue().getSuppliedBy(),
                        existingDocument.getValue().getUploadedBy(),
                        existingDocument.getValue().getDateTimeUploaded()
                    );

                    currentDocument = new IdValue<>(currentDocument.getId(), alteredDoc);

                    finalCurrentList.set(i, currentDocument);
                }
            }
        }
    }


    private List<AsylumCaseDefinition> getFieldDefinitions(boolean isReheardCase, boolean isOrWasAda) {
        List<AsylumCaseDefinition> fieldDefnList;
        if (isReheardCase) {
            fieldDefnList = new ArrayList<>(Arrays.asList(
                ADDITIONAL_EVIDENCE_DOCUMENTS,
                RESPONDENT_DOCUMENTS,
                FTPA_APPELLANT_DOCUMENTS,
                FTPA_RESPONDENT_DOCUMENTS,
                FINAL_DECISION_AND_REASONS_DOCUMENTS
            ));
        } else {
            fieldDefnList = new ArrayList<>(Arrays.asList(
                HEARING_DOCUMENTS,
                LEGAL_REPRESENTATIVE_DOCUMENTS,
                ADDITIONAL_EVIDENCE_DOCUMENTS,
                RESPONDENT_DOCUMENTS
            ));
            if (isOrWasAda) {
                fieldDefnList.add(TRIBUNAL_DOCUMENTS);
            }
        }
        return fieldDefnList;
    }

    private Map<AsylumCaseDefinition, AsylumCaseDefinition> getMappingFields(boolean isReheardCase, boolean isRemittedFeature, boolean isOrWasAda, boolean isUpdatedBundle) {
        Map<AsylumCaseDefinition, AsylumCaseDefinition> fieldMap;
        if (isReheardCase) {
            if (isRemittedFeature) {
                return Map.of(CUSTOM_FTPA_APPELLANT_DOCS, FTPA_APPELLANT_DOCUMENTS,
                    CUSTOM_FINAL_DECISION_AND_REASONS_DOCS, LATEST_DECISION_AND_REASONS_DOCUMENTS,
                    CUSTOM_REHEARD_HEARING_DOCS, LATEST_REHEARD_HEARING_DOCUMENTS,
                    CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS, APPELLANT_ADDENDUM_EVIDENCE_DOCS,
                    CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS, RESPONDENT_ADDENDUM_EVIDENCE_DOCS,
                    CUSTOM_LATEST_REMITTAL_DOCS, LATEST_REMITTAL_DOCUMENTS
                );
            }
            return Map.of(CUSTOM_APP_ADDITIONAL_EVIDENCE_DOCS, APP_ADDITIONAL_EVIDENCE_DOCS,
                CUSTOM_RESP_ADDITIONAL_EVIDENCE_DOCS, RESP_ADDITIONAL_EVIDENCE_DOCS,
                CUSTOM_FTPA_APPELLANT_DOCS, FTPA_APPELLANT_DOCUMENTS,
                CUSTOM_FTPA_RESPONDENT_DOCS, FTPA_RESPONDENT_DOCUMENTS,
                CUSTOM_FINAL_DECISION_AND_REASONS_DOCS, FINAL_DECISION_AND_REASONS_DOCUMENTS,
                CUSTOM_REHEARD_HEARING_DOCS, REHEARD_HEARING_DOCUMENTS,
                CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS, APPELLANT_ADDENDUM_EVIDENCE_DOCS,
                CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS, RESPONDENT_ADDENDUM_EVIDENCE_DOCS);
        } else {
            fieldMap = new HashMap<>(Map.of(CUSTOM_HEARING_DOCUMENTS, HEARING_DOCUMENTS,
                CUSTOM_LEGAL_REP_DOCUMENTS, LEGAL_REPRESENTATIVE_DOCUMENTS,
                CUSTOM_ADDITIONAL_EVIDENCE_DOCUMENTS, ADDITIONAL_EVIDENCE_DOCUMENTS,
                CUSTOM_RESPONDENT_DOCUMENTS, RESPONDENT_DOCUMENTS));
            if (isOrWasAda) {
                fieldMap.put(CUSTOM_TRIBUNAL_DOCUMENTS, TRIBUNAL_DOCUMENTS);
            }
            if (isUpdatedBundle) {
                fieldMap.put(CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS, APPELLANT_ADDENDUM_EVIDENCE_DOCS);
                fieldMap.put(CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS, RESPONDENT_ADDENDUM_EVIDENCE_DOCS);
            }
        }
        return fieldMap;
    }

    private Map<AsylumCaseDefinition, AsylumCaseDefinition> getMappingFieldsForAdditionalEvidenceDocuments() {
        return Map.of(CUSTOM_APP_ADDITIONAL_EVIDENCE_DOCS, ADDITIONAL_EVIDENCE_DOCUMENTS,
            CUSTOM_RESP_ADDITIONAL_EVIDENCE_DOCS, RESPONDENT_DOCUMENTS);
    }

    private List<IdValue<DocumentWithMetadata>> getIdValuesBefore(
        AsylumCase asylumCaseBefore,
        AsylumCaseDefinition fieldDefinition
    ) {

        if (asylumCaseBefore != null) {
            Optional<List<IdValue<DocumentWithMetadata>>> idValuesBeforeOptional = asylumCaseBefore
                .read(fieldDefinition);
            if (idValuesBeforeOptional.isPresent()) {
                return idValuesBeforeOptional.get();
            }
        }

        return Collections.emptyList();
    }

    private String getBundlePrefix(AsylumCase asylumCase) {

        final String appealReferenceNumber =
            asylumCase
                .read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)
                .orElseThrow(() -> new IllegalStateException("appealReferenceNumber is not present"));

        final String appellantFamilyName =
            asylumCase
                .read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)
                .orElseThrow(() -> new IllegalStateException("appellantFamilyName is not present"));

        return appealReferenceNumber.replace("/", " ")
            + "-" + appellantFamilyName;
    }

    private void prepareDocuments(Map<AsylumCaseDefinition, AsylumCaseDefinition> mappingFields, AsylumCase asylumCase) {

        mappingFields.forEach((sourceField, targetField) -> {

            if (asylumCase.read(sourceField).isEmpty()) {
                return;
            }

            List<IdValue<DocumentWithMetadata>> targetDocuments = getIdValuesBefore(asylumCase, targetField);

            Optional<List<IdValue<DocumentWithDescription>>> maybeDocuments =
                asylumCase.read(sourceField);

            List<IdValue<DocumentWithDescription>> documents =
                maybeDocuments.orElse(emptyList());

            List<IdValue<DocumentWithMetadata>> customDocuments = new ArrayList<>();

            if (!documents.isEmpty()) {

                for (IdValue<DocumentWithDescription> documentWithDescription : documents) {
                    //if the 'any document' is missing the tag, add the appropriate tag to it.
                    Optional<IdValue<DocumentWithMetadata>> maybeDocument = isDocumentWithDescriptionPresent(targetDocuments, documentWithDescription);
                    Document document = documentWithDescription.getValue().getDocument().orElseThrow(() -> new IllegalStateException(MISSING_DOCUMENT_EXCEPTION_MESSAGE));

                    DocumentWithMetadata newDocumentWithMetadata = null;

                    if (maybeDocument.isPresent()) {
                        DocumentTag maybeDocumentTag = maybeDocument.get().getValue().getTag();
                        if (maybeDocumentTag != DocumentTag.HEARING_BUNDLE &&
                            maybeDocumentTag != DocumentTag.UPDATED_HEARING_BUNDLE) {
                            newDocumentWithMetadata = new DocumentWithMetadata(document,
                                documentWithDescription.getValue().getDescription().orElse(""),
                                maybeDocument.get().getValue().getDateUploaded(),
                                maybeDocumentTag,
                                maybeDocument.get().getValue().getSuppliedBy(),
                                maybeDocument.get().getValue().getUploadedBy(),
                                maybeDocument.get().getValue().getDateTimeUploaded());
                        }
                    } else {
                        switch (sourceField) {
                            case CUSTOM_HEARING_DOCUMENTS:
                                newDocumentWithMetadata = new DocumentWithMetadata(document,
                                    documentWithDescription.getValue().getDescription().orElse(""),
                                    dateProvider.now().toString(),
                                    DocumentTag.HEARING_NOTICE,
                                    "",
                                    "",
                                    String.valueOf(dateProvider.nowWithTime()));
                                break;
                            case CUSTOM_REHEARD_HEARING_DOCS:
                                newDocumentWithMetadata = new DocumentWithMetadata(document,
                                    documentWithDescription.getValue().getDescription().orElse(""),
                                    dateProvider.now().toString(),
                                    DocumentTag.REHEARD_HEARING_NOTICE,
                                    "",
                                    "",
                                    String.valueOf(dateProvider.nowWithTime()));
                                break;
                            case CUSTOM_LEGAL_REP_DOCUMENTS:
                                newDocumentWithMetadata = new DocumentWithMetadata(document,
                                    documentWithDescription.getValue().getDescription().orElse(""),
                                    dateProvider.now().toString(),
                                    DocumentTag.CASE_ARGUMENT,
                                    "");
                                break;
                            case CUSTOM_ADDITIONAL_EVIDENCE_DOCUMENTS:
                                newDocumentWithMetadata = new DocumentWithMetadata(document,
                                    documentWithDescription.getValue().getDescription().orElse(""),
                                    dateProvider.now().toString(),
                                    DocumentTag.ADDITIONAL_EVIDENCE,
                                    "");
                                break;
                            case CUSTOM_RESP_ADDITIONAL_EVIDENCE_DOCS:
                                newDocumentWithMetadata = new DocumentWithMetadata(document,
                                    documentWithDescription.getValue().getDescription().orElse(""),
                                    dateProvider.now().toString(),
                                    DocumentTag.ADDITIONAL_EVIDENCE,
                                    SUPPLIED_BY_RESPONDENT);
                                break;
                            case CUSTOM_APP_ADDITIONAL_EVIDENCE_DOCS:
                                newDocumentWithMetadata = new DocumentWithMetadata(document,
                                    documentWithDescription.getValue().getDescription().orElse(""),
                                    dateProvider.now().toString(),
                                    DocumentTag.ADDITIONAL_EVIDENCE,
                                    SUPPLIED_BY_APPELLANT);
                                break;
                            case CUSTOM_RESPONDENT_DOCUMENTS:
                                newDocumentWithMetadata = new DocumentWithMetadata(document,
                                    documentWithDescription.getValue().getDescription().orElse(""),
                                    dateProvider.now().toString(),
                                    DocumentTag.RESPONDENT_EVIDENCE,
                                    "");
                                break;
                            case CUSTOM_FTPA_APPELLANT_DOCS:
                                newDocumentWithMetadata = new DocumentWithMetadata(document,
                                    documentWithDescription.getValue().getDescription().orElse(""),
                                    dateProvider.now().toString(),
                                    DocumentTag.FTPA_APPELLANT,
                                    "");
                                break;

                            case CUSTOM_FTPA_RESPONDENT_DOCS:
                                newDocumentWithMetadata = new DocumentWithMetadata(document,
                                    documentWithDescription.getValue().getDescription().orElse(""),
                                    dateProvider.now().toString(),
                                    DocumentTag.FTPA_RESPONDENT,
                                    "");
                                break;
                            case CUSTOM_FINAL_DECISION_AND_REASONS_DOCS:
                                newDocumentWithMetadata = new DocumentWithMetadata(document,
                                    documentWithDescription.getValue().getDescription().orElse(""),
                                    dateProvider.now().toString(),
                                    DocumentTag.FTPA_DECISION_AND_REASONS,
                                    "");
                                break;
                            case CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS:
                                newDocumentWithMetadata = new DocumentWithMetadata(document,
                                    documentWithDescription.getValue().getDescription().orElse(""),
                                    dateProvider.now().toString(),
                                    DocumentTag.ADDENDUM_EVIDENCE,
                                    SUPPLIED_BY_APPELLANT);
                                break;
                            case CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS:
                                newDocumentWithMetadata = new DocumentWithMetadata(document,
                                    documentWithDescription.getValue().getDescription().orElse(""),
                                    dateProvider.now().toString(),
                                    DocumentTag.ADDENDUM_EVIDENCE,
                                    SUPPLIED_BY_RESPONDENT);
                                break;
                            case CUSTOM_LATEST_REMITTAL_DOCS:
                                newDocumentWithMetadata = new DocumentWithMetadata(document,
                                    documentWithDescription.getValue().getDescription().orElse(""),
                                    dateProvider.now().toString(),
                                    DocumentTag.REMITTAL_DECISION,
                                    "");
                                break;
                            default:
                                break;
                        }
                    }
                    customDocuments = documentWithMetadataAppender.append(newDocumentWithMetadata, customDocuments);
                }
            }

            asylumCase.clear(targetField);
            asylumCase.write(targetField, customDocuments);
        });

    }

}
