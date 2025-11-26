package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealType.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

@Component
public class InternalDetMarkAsPaidLetterGenerator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> internalDetMarkAsPaidLetterLetterCreator;
    private final DocumentHandler documentHandler;

    public InternalDetMarkAsPaidLetterGenerator(
            @Qualifier("internalDetMarkAsPaidLetter") DocumentCreator<AsylumCase> internalDetMarkAsPaidLetterLetterCreator,
            DocumentHandler documentHandler
    ) {
        this.internalDetMarkAsPaidLetterLetterCreator = internalDetMarkAsPaidLetterLetterCreator;
        this.documentHandler = documentHandler;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        State currentState = callback.getCaseDetails().getState();

        boolean isCorrectAppealTypePA = asylumCase
                .read(APPEAL_TYPE, AppealType.class)
                .map(type -> type == PA).orElse(false);

        boolean isCorrectAppealTypeAndStateEaHuEu =
                isEaHuEuAppeal(asylumCase) && (currentState == State.APPEAL_SUBMITTED);

        Optional<PaymentStatus> paymentStatus = asylumCase
                .read(AsylumCaseDefinition.PAYMENT_STATUS, PaymentStatus.class);

        return callback.getEvent() == Event.MARK_APPEAL_PAID
                && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && (isCorrectAppealTypePA || isCorrectAppealTypeAndStateEaHuEu)
                && paymentStatus.isPresent()
                && paymentStatus.get().equals(PaymentStatus.PAID)
                && isInternalCase(asylumCase)
                && isAppellantInDetention(asylumCase)
                && !isAcceleratedDetainedAppeal(asylumCase);
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

        Document internalDetMarkAsPaidLetter = internalDetMarkAsPaidLetterLetterCreator.create(caseDetails);
        documentHandler.addWithMetadata(
                asylumCase,
                internalDetMarkAsPaidLetter,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.INTERNAL_DET_MARK_AS_PAID_LETTER
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
