package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.JourneyType.AIP;

import java.util.*;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.Appender;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;

@Component
public class CustomiseHearingBundlePreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final Appender<DocumentWithDescription> documentWithDescriptionAppender;
    private final FeatureToggler featureToggler;

    public CustomiseHearingBundlePreparer(Appender<DocumentWithDescription> documentWithDescriptionAppender,
                                          FeatureToggler featureToggler) {
        this.documentWithDescriptionAppender = documentWithDescriptionAppender;
        this.featureToggler = featureToggler;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
            && (callback.getEvent() == Event.CUSTOMISE_HEARING_BUNDLE
            || callback.getEvent() == Event.GENERATE_AMENDED_HEARING_BUNDLE);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        boolean isAmendedBundle = callback.getEvent() == Event.GENERATE_AMENDED_HEARING_BUNDLE;

        prepareCustomDocuments(asylumCase, isAmendedBundle);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    public void prepareCustomDocuments(AsylumCase asylumCase, boolean isAmendedBundle) {
        boolean isCaseReheard = asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false)
            && featureToggler.getValue("reheard-feature", false);
        boolean isAipJourney = asylumCase
            .read(JOURNEY_TYPE, JourneyType.class)
            .map(type -> type == AIP).orElse(false);
        boolean isOrWasAda = asylumCase.read(SUITABILITY_REVIEW_DECISION).isPresent();
        boolean isRemittedPath = asylumCase.read(SOURCE_OF_REMITTAL, String.class).isPresent();
        getMappingFields(isCaseReheard, isOrWasAda, isAmendedBundle, isRemittedPath).forEach((sourceField, targetField) ->
            populateCustomCollections(asylumCase, sourceField, targetField, isAipJourney)
        );
        // Map does not accept duplicate keys, so need to process this separately
        if (isCaseReheard || isAmendedBundle) {
            populateCustomCollections(asylumCase, ADDENDUM_EVIDENCE_DOCUMENTS, CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS, isAipJourney);
            if (isCaseReheard) {
                if (isRemittedPath) {
                    asylumCase.write(CUSTOM_LATEST_REMITTAL_DOCS, fetchLatestRemittalDocuments(asylumCase));
                }
                asylumCase.write(CUSTOM_REHEARD_HEARING_DOCS, fetchLatestReheardDocuments(asylumCase));
                asylumCase.write(CUSTOM_FINAL_DECISION_AND_REASONS_DOCS, fetchLatestDecisionDocuments(asylumCase));
            }
        }
    }

    private List<IdValue<DocumentWithDescription>> fetchLatestDecisionDocuments(AsylumCase asylumCase) {
        //Checking if we have some documents in Collection(Object) field, else get the updated/overwritten document from the FINAL field.
        Optional<List<IdValue<ReheardHearingDocuments>>> maybeExistingReheardDocuments =
            asylumCase.read(REHEARD_DECISION_REASONS_COLLECTION);
        List<IdValue<ReheardHearingDocuments>> allReheardDecisionDocuments = maybeExistingReheardDocuments
            .orElse(emptyList());

        if (allReheardDecisionDocuments.isEmpty()) {
            Optional<List<IdValue<DocumentWithMetadata>>> finalDEcisionAndResonDocs = asylumCase.read(FINAL_DECISION_AND_REASONS_DOCUMENTS);
            return getDocumentWithDescListFromMetaData(finalDEcisionAndResonDocs.orElse(emptyList()));
        } else {
            return getDocumentWithDescListFromMetaData(allReheardDecisionDocuments.get(0).getValue().getReheardHearingDocs());
        }
    }

    private List<IdValue<DocumentWithDescription>> fetchLatestRemittalDocuments(AsylumCase asylumCase) {
        Optional<List<IdValue<RemittalDocument>>> maybeExistingRemittalDocuments =
            asylumCase.read(REMITTAL_DOCUMENTS);
        List<IdValue<RemittalDocument>> allRemittalDocuments = maybeExistingRemittalDocuments
            .orElse(emptyList());

        if (!allRemittalDocuments.isEmpty()) {
            RemittalDocument remittalDocument = allRemittalDocuments.get(0).getValue();
            DocumentWithDescription remittalDocWithDesc = getDocumentWithDescFromMetaData(remittalDocument.getDecisionDocument());

            List<IdValue<DocumentWithDescription>> remittalOtherDocsWithDesc = getDocumentWithDescListFromMetaData(remittalDocument.getOtherRemittalDocs());

            return documentWithDescriptionAppender
                .append(remittalDocWithDesc, remittalOtherDocsWithDesc);
        }
        return emptyList();
    }

    private DocumentWithDescription getDocumentWithDescFromMetaData(DocumentWithMetadata documentWithMetadata) {
        return new DocumentWithDescription(documentWithMetadata.getDocument(),
            documentWithMetadata.getDescription());
    }

    private List<IdValue<DocumentWithDescription>> getDocumentWithDescListFromMetaData(List<IdValue<DocumentWithMetadata>> listDocumentWithMetaData) {
        List<IdValue<DocumentWithDescription>> listDocumentWithDesc = new ArrayList<>();
        for (IdValue<DocumentWithMetadata> documentWithMetadataIdValue : listDocumentWithMetaData) {
            listDocumentWithDesc = documentWithDescriptionAppender.append(
                getDocumentWithDescFromMetaData(documentWithMetadataIdValue.getValue()), listDocumentWithDesc);
        }
        return listDocumentWithDesc;
    }

    private List<IdValue<DocumentWithDescription>> fetchLatestReheardDocuments(AsylumCase asylumCase) {
        Optional<List<IdValue<ReheardHearingDocuments>>> maybeExistingReheardDocuments =
            asylumCase.read(REHEARD_HEARING_DOCUMENTS_COLLECTION);
        List<IdValue<ReheardHearingDocuments>> allReheardHearingDocuments = maybeExistingReheardDocuments
            .orElse(emptyList());

        List<IdValue<DocumentWithDescription>> reheardHearingDocsInCollection = emptyList();
        if (!allReheardHearingDocuments.isEmpty()) {
            reheardHearingDocsInCollection = getDocumentWithDescListFromMetaData(allReheardHearingDocuments.get(0).getValue().getReheardHearingDocs());
        }
        // Also if there were any documents prior to set-aside release in REHEARD_HEARING_DOCUMENTS take them into account as well.
        Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingReheardDocumentsPreSetAside =
            asylumCase.read(REHEARD_HEARING_DOCUMENTS);
        if (maybeExistingReheardDocumentsPreSetAside.isPresent()) {
            List<IdValue<DocumentWithDescription>> existingReheardDocumentsPreSetAside = getDocumentWithDescListFromMetaData(maybeExistingReheardDocumentsPreSetAside.get());
            for (IdValue<DocumentWithDescription> document : reheardHearingDocsInCollection) {
                existingReheardDocumentsPreSetAside = documentWithDescriptionAppender.append(document.getValue(), existingReheardDocumentsPreSetAside);
            }
            return existingReheardDocumentsPreSetAside;
        }
        return reheardHearingDocsInCollection;
    }

    private List<IdValue<DocumentWithDescription>> handleLegalRepSourceField(
        IdValue<DocumentWithMetadata> documentWithMetadata,
        DocumentWithDescription newDocumentWithDescription,
        List<IdValue<DocumentWithDescription>> customDocuments,
        boolean isAipJourney) {
        if (isAipJourney || (documentWithMetadata.getValue().getTag() == DocumentTag.APPEAL_SUBMISSION
            || documentWithMetadata.getValue().getTag() == DocumentTag.CASE_ARGUMENT)) {
            return documentWithDescriptionAppender.append(newDocumentWithDescription, customDocuments);
        } else {
            return customDocuments;
        }
    }

    private List<IdValue<DocumentWithDescription>> handleCustomAddendumDocsTargetField(
        IdValue<DocumentWithMetadata> documentWithMetadata,
        DocumentWithDescription newDocumentWithDescription,
        List<IdValue<DocumentWithDescription>> customDocuments,
        String user) {
        if (!user.equals("The appellant") && !user.equals("The respondent")) {
            throw new IllegalArgumentException("Invalid user");
        }
        if (user.equals(documentWithMetadata.getValue().getSuppliedBy())) {
            return documentWithDescriptionAppender.append(newDocumentWithDescription, customDocuments);
        } else {
            return customDocuments;
        }
    }

    private List<IdValue<DocumentWithDescription>> handleCustomTribunalDocsTargetField(
        IdValue<DocumentWithMetadata> documentWithMetadata,
        DocumentWithDescription newDocumentWithDescription,
        List<IdValue<DocumentWithDescription>> customDocuments) {
        if (documentWithMetadata.getValue().getTag() == DocumentTag.ADA_SUITABILITY) {
            return documentWithDescriptionAppender.append(newDocumentWithDescription, customDocuments);
        } else {
            return customDocuments;
        }
    }

    void populateCustomCollections(AsylumCase asylumCase, AsylumCaseDefinition sourceField, AsylumCaseDefinition targetField, boolean isAipJourney) {
        if (asylumCase.read(sourceField).isEmpty()) {
            return;
        }

        Optional<List<IdValue<DocumentWithMetadata>>> maybeDocuments =
            asylumCase.read(sourceField);

        List<IdValue<DocumentWithMetadata>> documents =
            maybeDocuments.orElse(emptyList());

        List<IdValue<DocumentWithDescription>> customDocuments = new ArrayList<>();

        for (IdValue<DocumentWithMetadata> documentWithMetadata : documents) {
            if (documentWithMetadata.getValue().getTag() == DocumentTag.HEARING_BUNDLE) {
                continue;
            }
            DocumentWithDescription newDocumentWithDescription =
                new DocumentWithDescription(documentWithMetadata.getValue().getDocument(),
                    documentWithMetadata.getValue().getDescription());

            if (sourceField == LEGAL_REPRESENTATIVE_DOCUMENTS) {
                customDocuments = handleLegalRepSourceField(documentWithMetadata, newDocumentWithDescription, customDocuments, isAipJourney);
            } else {
                customDocuments = switch (targetField) {
                    case CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS:
                        yield handleCustomAddendumDocsTargetField(documentWithMetadata, newDocumentWithDescription, customDocuments, "The appellant");
                    case CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS:
                        yield handleCustomAddendumDocsTargetField(documentWithMetadata, newDocumentWithDescription, customDocuments, "The respondent");
                    case CUSTOM_TRIBUNAL_DOCUMENTS:
                        yield handleCustomTribunalDocsTargetField(documentWithMetadata, newDocumentWithDescription, customDocuments);
                    default:
                        yield documentWithDescriptionAppender.append(newDocumentWithDescription, customDocuments);
                };
            }
        }

        asylumCase.clear(targetField);
        asylumCase.write(targetField, customDocuments);

    }

    private Map<AsylumCaseDefinition, AsylumCaseDefinition> getMappingFields(boolean isReheardCase, boolean isOrWasAda, boolean isAmendedBundle, boolean isRemittedFeature) {
        Map<AsylumCaseDefinition, AsylumCaseDefinition> fieldMapping;
        if (isReheardCase) {
            fieldMapping = new HashMap<>(Map.of(
                FTPA_APPELLANT_DOCUMENTS, CUSTOM_FTPA_APPELLANT_DOCS,
                REHEARD_HEARING_DOCUMENTS, CUSTOM_REHEARD_HEARING_DOCS,
                ADDENDUM_EVIDENCE_DOCUMENTS, CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS));
            if (!isRemittedFeature) {
                fieldMapping.put(APP_ADDITIONAL_EVIDENCE_DOCS, CUSTOM_APP_ADDITIONAL_EVIDENCE_DOCS);
                fieldMapping.put(RESP_ADDITIONAL_EVIDENCE_DOCS, CUSTOM_RESP_ADDITIONAL_EVIDENCE_DOCS);
                fieldMapping.put(FTPA_RESPONDENT_DOCUMENTS, CUSTOM_FTPA_RESPONDENT_DOCS);
                fieldMapping.put(FINAL_DECISION_AND_REASONS_DOCUMENTS, CUSTOM_FINAL_DECISION_AND_REASONS_DOCS);
            }
        } else {
            fieldMapping = new HashMap<>(Map.of(
                HEARING_DOCUMENTS, CUSTOM_HEARING_DOCUMENTS,
                LEGAL_REPRESENTATIVE_DOCUMENTS, CUSTOM_LEGAL_REP_DOCUMENTS,
                ADDITIONAL_EVIDENCE_DOCUMENTS, CUSTOM_ADDITIONAL_EVIDENCE_DOCUMENTS,
                RESPONDENT_DOCUMENTS, CUSTOM_RESPONDENT_DOCUMENTS));
            if (isAmendedBundle) {
                fieldMapping.put(ADDENDUM_EVIDENCE_DOCUMENTS, CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS);
            }
            if (isOrWasAda) {
                //With Tribunal Documents
                fieldMapping.put(TRIBUNAL_DOCUMENTS, CUSTOM_TRIBUNAL_DOCUMENTS);
            }
        }
        return fieldMapping;
    }

}
