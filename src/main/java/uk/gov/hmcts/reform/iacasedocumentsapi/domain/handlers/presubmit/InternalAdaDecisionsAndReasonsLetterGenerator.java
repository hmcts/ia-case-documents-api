package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealDecision;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

@Component
public class InternalAdaDecisionsAndReasonsLetterGenerator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> internalAdaDecisionsAndReasonsLetterAllowedCreator;
    private final DocumentCreator<AsylumCase> internalAdaDecisionsAndReasonsLetterDismissedCreator;
    private final DocumentHandler documentHandler;

    public InternalAdaDecisionsAndReasonsLetterGenerator(
            @Qualifier("internalAdaDecisionsAndReasonsAllowed") DocumentCreator<AsylumCase> internalAdaDecisionsAndReasonsLetterAllowedCreator,
            @Qualifier("internalAdaDecisionsAndReasonsDismissed") DocumentCreator<AsylumCase> internalAdaDecisionsAndReasonsLetterDismissedCreator,
            DocumentHandler documentHandler
    ) {
        this.internalAdaDecisionsAndReasonsLetterAllowedCreator = internalAdaDecisionsAndReasonsLetterAllowedCreator;
        this.internalAdaDecisionsAndReasonsLetterDismissedCreator = internalAdaDecisionsAndReasonsLetterDismissedCreator;
        this.documentHandler = documentHandler;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return callback.getEvent() == Event.SEND_DECISION_AND_REASONS
                && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && isInternalCase(asylumCase)
                && isAcceleratedDetainedAppeal(asylumCase);
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


        final AppealDecision appealDecision =
                asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)
                        .orElseThrow(() -> new RequiredFieldMissingException("Appeal decision is missing."));

        if (appealDecision.equals(AppealDecision.ALLOWED)) {
            documentHandler.addWithMetadata(
                    asylumCase,
                    internalAdaDecisionsAndReasonsLetterAllowedCreator.create(caseDetails),
                    NOTIFICATION_ATTACHMENT_DOCUMENTS,
                    DocumentTag.INTERNAL_ADA_DECISION_AND_REASONS_LETTER
            );
        } else {
            documentHandler.addWithMetadata(
                    asylumCase,
                    internalAdaDecisionsAndReasonsLetterDismissedCreator.create(caseDetails),
                    NOTIFICATION_ATTACHMENT_DOCUMENTS,
                    DocumentTag.INTERNAL_ADA_DECISION_AND_REASONS_LETTER
            );
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
