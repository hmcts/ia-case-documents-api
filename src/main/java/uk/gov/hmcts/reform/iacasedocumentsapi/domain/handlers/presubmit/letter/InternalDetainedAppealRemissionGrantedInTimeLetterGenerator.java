package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LATE_REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.IRC;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.PRISON;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.PaymentStatus.PAID;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;

@Component
public class InternalDetainedAppealRemissionGrantedInTimeLetterGenerator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> documentCreator;
    private final DocumentHandler documentHandler;

    public InternalDetainedAppealRemissionGrantedInTimeLetterGenerator(
            @Qualifier("internalDetainedAppealRemissionGrantedInTimeLetter") DocumentCreator<AsylumCase> documentCreator,
            DocumentHandler documentHandler
    ) {
        this.documentCreator = documentCreator;
        this.documentHandler = documentHandler;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        Objects.requireNonNull(callbackStage, "callbackStage must not be null");
        Objects.requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final Optional<CaseDetails<AsylumCase>> caseDetailsBefore = callback.getCaseDetailsBefore();

        boolean submissionInTime = asylumCase
                .read(AsylumCaseDefinition.SUBMISSION_OUT_OF_TIME, YesOrNo.class)
                .map(yesOrNo -> yesOrNo == YesOrNo.NO)
                .orElse(false);

        boolean paymentPaid = caseDetailsBefore.isPresent()
                ? caseDetailsBefore.get().getCaseData().read(AsylumCaseDefinition.PAYMENT_STATUS, PaymentStatus.class)
                .map(paymentStatus -> paymentStatus == PAID).orElse(false) : false;

        Optional<RemissionType> lateRemissionType = asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class);

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.RECORD_REMISSION_DECISION
                && isInternalCase(asylumCase) && hasAppealBeenSubmittedByAppellantInternalCase(asylumCase)
                && submissionInTime
                && !paymentPaid
                && isRemissionApproved(asylumCase)
                && isDetainedInOneOfFacilityTypes(asylumCase, IRC, PRISON)
                && lateRemissionType.isEmpty()
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

        Document internalDetainedAppealRemissionGrantedInTimeLetterLetter = documentCreator.create(caseDetails);

        documentHandler.addWithMetadata(
                asylumCase,
                internalDetainedAppealRemissionGrantedInTimeLetterLetter,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.INTERNAL_DETAINED_APPEAL_REMISSION_GRANTED_IN_TIME_LETTER
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
