package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;

import java.util.Arrays;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

@Component
public class AppealSubmissionCreator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> appealSubmissionDocumentCreator;
    private final DocumentCreator<AsylumCase> internalAppealSubmissionDocumentCreator;
    private final DocumentHandler documentHandler;

    public AppealSubmissionCreator(
        @Qualifier("appealSubmission") DocumentCreator<AsylumCase> appealSubmissionDocumentCreator,
        @Qualifier("internalAppealSubmission") DocumentCreator<AsylumCase> internalAppealSubmissionDocumentCreator,
        DocumentHandler documentHandler
    ) {
        this.appealSubmissionDocumentCreator = appealSubmissionDocumentCreator;
        this.internalAppealSubmissionDocumentCreator = internalAppealSubmissionDocumentCreator;
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

        boolean paymentFailed =
            asylumCase
                .read(AsylumCaseDefinition.PAYMENT_STATUS, PaymentStatus.class)
                .map(paymentStatus -> paymentStatus == FAILED).orElse(false);

        boolean payLater =
            asylumCase
                .read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)
                .map(paymentOption -> paymentOption.equals("payOffline") || paymentOption.equals("payLater")).orElse(false);

        boolean paymentFailedChangedToPayLater = paymentFailed && payLater;

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && Arrays.asList(
                    Event.SUBMIT_APPEAL,
                    Event.EDIT_APPEAL_AFTER_SUBMIT,
                    Event.PAY_AND_SUBMIT_APPEAL)
                   .contains(callback.getEvent())
               && (!paymentFailed || paymentFailedChangedToPayLater);
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

        appealSubmission = appealSubmissionDocumentCreator.create(caseDetails);
        documentTag = DocumentTag.APPEAL_SUBMISSION;
        documentField = LEGAL_REPRESENTATIVE_DOCUMENTS;

        documentHandler.addWithMetadata(
            asylumCase,
            appealSubmission,
            documentField,
            documentTag
        );

        if (callback.getEvent().equals(Event.SUBMIT_APPEAL) && isInternalCase(asylumCase) && isAppellantInDetention(asylumCase) && !isAcceleratedDetainedAppeal(asylumCase)) {
            appealSubmission = internalAppealSubmissionDocumentCreator.create(caseDetails);
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

