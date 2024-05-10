package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealReviewOutcome;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

@Component
public class InternalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterGenerator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetter;
    private final DocumentHandler documentHandler;

    public InternalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterGenerator(
            @Qualifier("internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetter") DocumentCreator<AsylumCase> internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetter,
            DocumentHandler documentHandler
    ) {
        this.internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetter = internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetter;
        this.documentHandler = documentHandler;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return callback.getEvent() == Event.REQUEST_RESPONSE_REVIEW
                && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && isInternalCase(asylumCase)
                && !isAcceleratedDetainedAppeal(asylumCase)
                && isAppellantInDetention(asylumCase)
                && getAppealReviewOutcome(asylumCase).equals(AppealReviewOutcome.DECISION_WITHDRAWN);
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

        Document uploadTheAppealResponseLetter = internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetter.create(caseDetails);
        documentHandler.addWithMetadata(
                asylumCase,
                uploadTheAppealResponseLetter,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.INTERNAL_DETAINED_REQUEST_HO_RESPONSE_REVIEW
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private AppealReviewOutcome getAppealReviewOutcome(AsylumCase asylumCase) {
        return asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class)
                .orElseThrow(() -> new IllegalStateException("Appeal review outcome is not present"));
    }
}
