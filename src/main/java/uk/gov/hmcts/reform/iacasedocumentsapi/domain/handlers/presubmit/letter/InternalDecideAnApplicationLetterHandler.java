package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.MakeAnApplicationService;


@Component
public class InternalDecideAnApplicationLetterHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> internalDecideAnApplicationDecisionGrantedLetter;
    private final DocumentCreator<AsylumCase> internalDecideAnApplicationDecisionRefusedLetter;
    private final DocumentHandler documentHandler;
    private final MakeAnApplicationService makeAnApplicationService;
    private final String decisionGranted = "Granted";
    private final String decisionRefused = "Refused";

    public InternalDecideAnApplicationLetterHandler(
            @Qualifier("internalDecideAnApplicationDecisionGrantedLetter") DocumentCreator<AsylumCase> internalDecideAnApplicationDecisionGrantedLetter,
            @Qualifier("internalDecideAnApplicationDecisionRefusedLetter") DocumentCreator<AsylumCase> internalDecideAnApplicationDecisionRefusedLetter,
            DocumentHandler documentHandler,
            MakeAnApplicationService makeAnApplicationService
    ) {
        this.internalDecideAnApplicationDecisionGrantedLetter = internalDecideAnApplicationDecisionGrantedLetter;
        this.internalDecideAnApplicationDecisionRefusedLetter = internalDecideAnApplicationDecisionRefusedLetter;
        this.documentHandler = documentHandler;
        this.makeAnApplicationService = makeAnApplicationService;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return callback.getEvent() == Event.DECIDE_AN_APPLICATION
                && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && isInternalCase(asylumCase)
                && isAppellantInDetention(asylumCase);
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

        Optional<MakeAnApplication> optionalMakeAnApplication = makeAnApplicationService.getMakeAnApplication(asylumCase, true);
        if (!optionalMakeAnApplication.isPresent()) {
            throw new IllegalStateException("Application not found");
        }
        boolean applicationGranted = optionalMakeAnApplication.get().getDecision().equals(decisionGranted);
        boolean applicationRefused = optionalMakeAnApplication.get().getDecision().equals(decisionRefused);

        Document documentForUpload;
        if (applicationGranted) {
            documentForUpload = internalDecideAnApplicationDecisionGrantedLetter.create(caseDetails);
        } else if (applicationRefused) {
            documentForUpload = internalDecideAnApplicationDecisionRefusedLetter.create(caseDetails);
        } else {
            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        documentHandler.addWithMetadata(
                asylumCase,
                documentForUpload,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.INTERNAL_DECIDE_AN_APPLICATION_LETTER
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
