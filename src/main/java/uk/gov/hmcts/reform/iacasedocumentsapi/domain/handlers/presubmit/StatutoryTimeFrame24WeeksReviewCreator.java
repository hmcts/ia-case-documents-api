package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
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

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isInternalCase;

@Slf4j
@Component
public class StatutoryTimeFrame24WeeksReviewCreator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> statutoryTimeFrame24WeeksReviewDocumentCreator;
    private final DocumentHandler documentHandler;

    public StatutoryTimeFrame24WeeksReviewCreator(
            @Qualifier("statutoryTimeFrame24WeeksReview") DocumentCreator<AsylumCase> statutoryTimeFrame24WeeksReviewDocumentCreator,
            DocumentHandler documentHandler
    ) {
        this.statutoryTimeFrame24WeeksReviewDocumentCreator = statutoryTimeFrame24WeeksReviewDocumentCreator;
        this.documentHandler = documentHandler;
    }


    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        final AsylumCase asylumCase =
                callback
                        .getCaseDetails()
                        .getCaseData();

        boolean canHandleReviewDoc = callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                &&
                Event.COMPLETE_CASE_REVIEW
                        .equals(callback.getEvent());
        log.info("canHandleReviewDoc {}", canHandleReviewDoc);
        return canHandleReviewDoc;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {

        final CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        final AsylumCase asylumCase = caseDetails.getCaseData();

        Document appealSubmission;
        DocumentTag documentTag;
        AsylumCaseDefinition documentField;


        boolean canAddDocument = callback.getEvent().equals(Event.COMPLETE_CASE_REVIEW) && isInternalCase(asylumCase);
        log.info("canAddDocument {}", canAddDocument);
        if (canAddDocument) {
            appealSubmission = statutoryTimeFrame24WeeksReviewDocumentCreator.create(caseDetails);
            documentTag = DocumentTag.INTERNAL_APPEAL_SUBMISSION;
            documentField = NOTIFICATION_ATTACHMENT_DOCUMENTS;

            documentHandler.addWithMetadata(
                    asylumCase,
                    appealSubmission,
                    documentField,
                    documentTag
            );
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}

