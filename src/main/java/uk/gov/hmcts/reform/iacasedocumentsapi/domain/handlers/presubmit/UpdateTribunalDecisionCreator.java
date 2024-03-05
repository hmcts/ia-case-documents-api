package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UpdateTribunalRules.UNDER_RULE_31;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.JourneyType.AIP;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DecisionAndReasons;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UpdateTribunalRules;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

@Component
public class UpdateTribunalDecisionCreator implements PreSubmitCallbackHandler<AsylumCase> {
    private final DocumentCreator<AsylumCase> updatedDecisionAndReasonsCoverLetterDocumentCreator;
    private final DocumentCreator<AsylumCase> aipUpdatedDecisionAndReasonsCoverLetterDocumentCreator;
    private final DocumentHandler documentHandler;
    private final DateProvider dateProvider;


    public UpdateTribunalDecisionCreator(
            @Qualifier("updatedDecisionAndReasonsCoverLetter") DocumentCreator<AsylumCase> updatedDecisionAndReasonsCoverLetterDocumentCreator,
            @Qualifier("aipUpdatedDecisionAndReasonsCoverLetter") DocumentCreator<AsylumCase> aipUpdatedDecisionAndReasonsCoverLetterDocumentCreator,
            DocumentHandler documentHandler,
            DateProvider dateProvider
    ) {
        this.updatedDecisionAndReasonsCoverLetterDocumentCreator = updatedDecisionAndReasonsCoverLetterDocumentCreator;
        this.aipUpdatedDecisionAndReasonsCoverLetterDocumentCreator = aipUpdatedDecisionAndReasonsCoverLetterDocumentCreator;
        this.documentHandler = documentHandler;
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && isDecisionRule31(callback.getCaseDetails().getCaseData())
                && callback.getEvent() == Event.UPDATE_TRIBUNAL_DECISION;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        final AsylumCase asylumCase = caseDetails.getCaseData();
        Document coverLetter = getCoverLetterCreatorInstance(asylumCase).create(caseDetails);

        documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                asylumCase,
                coverLetter,
                FINAL_DECISION_AND_REASONS_DOCUMENTS,
                DocumentTag.UPDATED_DECISION_AND_REASONS_COVER_LETTER
        );

        Optional<List<IdValue<DecisionAndReasons>>> maybeExistingDecisionAndReasons =
                asylumCase.read(CORRECTED_DECISION_AND_REASONS);

        List<IdValue<DecisionAndReasons>> existingDecisionAndReasons = maybeExistingDecisionAndReasons.orElse(emptyList());

        DecisionAndReasons decisionAndReasons = existingDecisionAndReasons.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("updatedDecisionAndReasons is not present in correctedDecisionAndReasons list"))
                .getValue();

        decisionAndReasons.setCoverLetterDocument(coverLetter);
        decisionAndReasons.setDateCoverLetterDocumentUploaded(dateProvider.now().toString());
        asylumCase.write(CORRECTED_DECISION_AND_REASONS, existingDecisionAndReasons);
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isDecisionRule31(AsylumCase asylumCase) {
        return asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, UpdateTribunalRules.class)
                .map(type -> type.equals(UNDER_RULE_31)).orElse(false);
    }

    private DocumentCreator<AsylumCase> getCoverLetterCreatorInstance(AsylumCase asylumCase) {
        boolean isAipJourney = asylumCase.read(JOURNEY_TYPE, JourneyType.class)
                .map(type -> type == AIP).orElse(false);

        return isAipJourney ? aipUpdatedDecisionAndReasonsCoverLetterDocumentCreator :
                updatedDecisionAndReasonsCoverLetterDocumentCreator;
    }
}
