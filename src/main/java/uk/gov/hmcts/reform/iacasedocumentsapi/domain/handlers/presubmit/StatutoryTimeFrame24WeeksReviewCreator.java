package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isInternalCase;

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

    private static boolean hasStf24WeeksStatus(AsylumCase asylumCase) {
        final YesOrNo status = asylumCase
                .read(AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)
                .orElseThrow(() -> new IllegalStateException("STF 24W CURRENT STATUS AUTO GENERATED is not present"));
        return status.equals(YES);
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

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                &&
                Event.COMPLETE_CASE_REVIEW
                        .equals(callback.getEvent()) && hasStf24WeeksStatus(asylumCase);
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

        Document appealSubmission;
        DocumentTag documentTag;
        AsylumCaseDefinition documentField;


        if (callback.getEvent().equals(Event.SUBMIT_APPEAL) && isInternalCase(asylumCase)) {
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

