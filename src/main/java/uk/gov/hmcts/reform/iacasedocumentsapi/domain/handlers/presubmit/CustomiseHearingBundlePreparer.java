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
        getMappingFields(isCaseReheard, isOrWasAda, isAmendedBundle).forEach((sourceField,targetField)  ->
            populateCustomCollections(asylumCase, sourceField, targetField, isAipJourney)
        );

        // Map does not accept duplicate keys, so need to process this separately
        if (isCaseReheard || isAmendedBundle) {
            populateCustomCollections(asylumCase,ADDENDUM_EVIDENCE_DOCUMENTS, CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS, isAipJourney);
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
            if (sourceField == HEARING_DOCUMENTS &&
                documentWithMetadata.getValue().getTag() == DocumentTag.HEARING_BUNDLE) {
                continue;
            }
            DocumentWithDescription newDocumentWithDescription =
                new DocumentWithDescription(documentWithMetadata.getValue().getDocument(),
                    documentWithMetadata.getValue().getDescription());

            if (sourceField == LEGAL_REPRESENTATIVE_DOCUMENTS) {
                if (isAipJourney || (documentWithMetadata.getValue().getTag() == DocumentTag.APPEAL_SUBMISSION
                    || documentWithMetadata.getValue().getTag() == DocumentTag.CASE_ARGUMENT)) {
                    customDocuments = documentWithDescriptionAppender.append(newDocumentWithDescription, customDocuments);
                }
            } else if (targetField == CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS) {
                if ("The appellant".equals(documentWithMetadata.getValue().getSuppliedBy())) {
                    customDocuments = documentWithDescriptionAppender.append(newDocumentWithDescription, customDocuments);
                }
            } else if (targetField == CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS) {
                if ("The respondent".equals(documentWithMetadata.getValue().getSuppliedBy())) {
                    customDocuments = documentWithDescriptionAppender.append(newDocumentWithDescription, customDocuments);
                }
            } else if (targetField == CUSTOM_TRIBUNAL_DOCUMENTS) {
                if (documentWithMetadata.getValue().getTag() == DocumentTag.ADA_SUITABILITY) {
                    customDocuments = documentWithDescriptionAppender.append(newDocumentWithDescription, customDocuments);
                }
            } else {
                customDocuments = documentWithDescriptionAppender.append(newDocumentWithDescription, customDocuments);
            }
        }

        asylumCase.clear(targetField);
        asylumCase.write(targetField, customDocuments);

    }

    private Map<AsylumCaseDefinition,AsylumCaseDefinition> getMappingFields(boolean isReheardCase, boolean isOrWasAda, boolean isAmendedBundle) {
        Map<AsylumCaseDefinition, AsylumCaseDefinition> fieldMapping;
        if (isReheardCase) {
            fieldMapping = new HashMap<>(Map.of(
                APP_ADDITIONAL_EVIDENCE_DOCS, CUSTOM_APP_ADDITIONAL_EVIDENCE_DOCS,
                RESP_ADDITIONAL_EVIDENCE_DOCS, CUSTOM_RESP_ADDITIONAL_EVIDENCE_DOCS,
                FTPA_APPELLANT_DOCUMENTS, CUSTOM_FTPA_APPELLANT_DOCS,
                FTPA_RESPONDENT_DOCUMENTS, CUSTOM_FTPA_RESPONDENT_DOCS,
                FINAL_DECISION_AND_REASONS_DOCUMENTS, CUSTOM_FINAL_DECISION_AND_REASONS_DOCS,
                REHEARD_HEARING_DOCUMENTS, CUSTOM_REHEARD_HEARING_DOCS,
                ADDENDUM_EVIDENCE_DOCUMENTS, CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS));

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
