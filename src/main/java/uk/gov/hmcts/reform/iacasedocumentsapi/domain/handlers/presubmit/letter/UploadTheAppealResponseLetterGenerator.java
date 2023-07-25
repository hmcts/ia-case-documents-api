package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REVIEW_OUTCOME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils;

@Component
public class UploadTheAppealResponseLetterGenerator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> uploadTheAppealResponseLetterCreator;
    private final DocumentHandler documentHandler;

    public UploadTheAppealResponseLetterGenerator(
            @Qualifier("uploadTheAppealResponseLetter") DocumentCreator<AsylumCase> uploadTheAppealResponseLetterCreator,
            DocumentHandler documentHandler
    ) {
        this.uploadTheAppealResponseLetterCreator = uploadTheAppealResponseLetterCreator;
        this.documentHandler = documentHandler;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return callback.getEvent() == Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE
                && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && AsylumCaseUtils.isInternalCase(asylumCase)
                && AsylumCaseUtils.isAcceleratedDetainedAppeal(asylumCase)
                && getAppealReviewOutcome(asylumCase).equals("decisionMaintained");
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

        Document uploadTheAppealResponseLetter = uploadTheAppealResponseLetterCreator.create(caseDetails);
        documentHandler.addWithMetadata(
                asylumCase,
                uploadTheAppealResponseLetter,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.UPLOAD_THE_APPEAL_RESPONSE
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private String getAppealReviewOutcome(AsylumCase asylumCase) {
        AppealReviewOutcome appealReviewOutcome = asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class)
                .orElseThrow(() -> new IllegalStateException("Appeal review outcome is not present"));

        return appealReviewOutcome.toString();
    }
}
