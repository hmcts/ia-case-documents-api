package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;


import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealType.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils;


@Component
public class InternalDetainedNoRemissionPaymentDueLetter implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> internalDetainedNoRemissionPaymentDueCreator;
    private final DocumentHandler documentHandler;

    public InternalDetainedNoRemissionPaymentDueLetter(
            @Qualifier("internalDetainedNoRemissionPaymentDue") DocumentCreator<AsylumCase> internalDetainedNoRemissionPaymentDueCreator,
            DocumentHandler documentHandler
    ) {
        this.internalDetainedNoRemissionPaymentDueCreator = internalDetainedNoRemissionPaymentDueCreator;
        this.documentHandler = documentHandler;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String appealType = asylumCase.read(APPEAL_TYPE, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException("Appeal type not found"));
        boolean isHuEaEu = List.of(HU.getValue(), EA.getValue(), EU.getValue()).contains(appealType);

        boolean isNoRemission = asylumCase.read(REMISSION_TYPE, RemissionType.class)
                .map(remission -> remission == RemissionType.NO_REMISSION).orElse(false);

        return callback.getEvent() == Event.SUBMIT_APPEAL
                && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getCaseDetails().getState().equals(State.PENDING_PAYMENT)
                && AsylumCaseUtils.isInternalCase(asylumCase)
                && AsylumCaseUtils.isAppellantInDetention(asylumCase)
                && !isAcceleratedDetainedAppeal(asylumCase)
                && isNoRemission
                && isHuEaEu;
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

        Document internalDetainedNoRemissionPaymentDueLetter = internalDetainedNoRemissionPaymentDueCreator.create(caseDetails);
        documentHandler.addWithMetadata(
                asylumCase,
                internalDetainedNoRemissionPaymentDueLetter,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.INTERNAL_APPEAL_FEE_DUE_LETTER
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
