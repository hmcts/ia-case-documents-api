package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isInternalCase;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.STF_24_WEEKS_REVIEW_DOCUMENT_CREATOR;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.isCaseReviewFor24WeeksCase;

@Slf4j
@Component
public class StatutoryTimeFrame24WeeksReviewCreator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> stf24WeeksReviewDocumentCreator;
    private final DocumentHandler documentHandler;

    public StatutoryTimeFrame24WeeksReviewCreator(
            @Qualifier(STF_24_WEEKS_REVIEW_DOCUMENT_CREATOR) DocumentCreator<AsylumCase> stf24WeeksReviewDocumentCreator,
            DocumentHandler documentHandler) {
        this.stf24WeeksReviewDocumentCreator = stf24WeeksReviewDocumentCreator;
        this.documentHandler = documentHandler;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        boolean canHandleReviewDoc = callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && isCaseReviewFor24WeeksCase(callback.getEvent(), asylumCase)
                && !isInternalCase(asylumCase);
        log.info("{} canHandle Review Doc {}", STF_24_WEEKS_REVIEW_DOCUMENT_CREATOR, canHandleReviewDoc);
        return canHandleReviewDoc;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback) {

        final CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        final AsylumCase asylumCase = caseDetails.getCaseData();

        boolean canAddDocument = isCaseReviewFor24WeeksCase(callback.getEvent(), asylumCase) && !isInternalCase(asylumCase);
        log.info("{} canAddDocument Doc {}", STF_24_WEEKS_REVIEW_DOCUMENT_CREATOR, canAddDocument);
        if (canAddDocument) {
            Document appealSubmission = stf24WeeksReviewDocumentCreator.create(caseDetails);
            documentHandler.addWithMetadata(
                    asylumCase,
                    appealSubmission,
                    NOTIFICATION_ATTACHMENT_DOCUMENTS,
                    DocumentTag.INTERNAL_APPEAL_SUBMISSION
            );
            log.info("{} doc added successfully for tag {}", STF_24_WEEKS_REVIEW_DOCUMENT_CREATOR, DocumentTag.INTERNAL_APPEAL_SUBMISSION);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}

