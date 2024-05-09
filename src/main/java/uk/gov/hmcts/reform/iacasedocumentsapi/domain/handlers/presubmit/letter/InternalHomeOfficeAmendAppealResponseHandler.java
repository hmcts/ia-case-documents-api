package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag.AMEND_HOME_OFFICE_APPEAL_RESPONSE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.DirectionFinder;


@Component
public class InternalHomeOfficeAmendAppealResponseHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> internalHomeOfficeAmendAppealResponseLetter;
    private final DocumentHandler documentHandler;
    private final DirectionFinder directionFinder;

    public InternalHomeOfficeAmendAppealResponseHandler(
            @Qualifier("internalHomeOfficeAmendAppealResponseLetter") DocumentCreator<AsylumCase> internalHomeOfficeAmendAppealResponseLetter,
            DocumentHandler documentHandler,
            DirectionFinder directionFinder
    ) {
        this.internalHomeOfficeAmendAppealResponseLetter = internalHomeOfficeAmendAppealResponseLetter;
        this.documentHandler = documentHandler;
        this.directionFinder = directionFinder;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return callback.getEvent() == Event.REQUEST_RESPONSE_AMEND
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

        documentHandler.addWithMetadata(
                asylumCase,
                internalHomeOfficeAmendAppealResponseLetter.create(caseDetails),
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                AMEND_HOME_OFFICE_APPEAL_RESPONSE
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
