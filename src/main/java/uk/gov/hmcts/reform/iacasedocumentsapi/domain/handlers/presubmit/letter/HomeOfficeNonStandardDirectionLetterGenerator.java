package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAppellantInDetention;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isInternalCase;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.DirectionFinder;

@Component
public class HomeOfficeNonStandardDirectionLetterGenerator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> homeOfficeNonStandardDirectionLetterCreator;
    private final DocumentHandler documentHandler;
    private final DirectionFinder directionFinder;

    public HomeOfficeNonStandardDirectionLetterGenerator(
        @Qualifier("homeOfficeNonStandardDirectionLetter")
        DocumentCreator<AsylumCase> homeOfficeNonStandardDirectionLetterCreator,
        DocumentHandler documentHandler,
        DirectionFinder directionFinder) {
        this.homeOfficeNonStandardDirectionLetterCreator = homeOfficeNonStandardDirectionLetterCreator;
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

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.SEND_DIRECTION
               && isInternalCase(asylumCase)
               && isAppellantInDetention(asylumCase)
               && isRecipientRespondent(asylumCase);
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

        Document uploadDocument = homeOfficeNonStandardDirectionLetterCreator.create(caseDetails);
        documentHandler.addWithMetadata(
            asylumCase,
            uploadDocument,
            NOTIFICATION_ATTACHMENT_DOCUMENTS,
            DocumentTag.HOME_OFFICE_NON_STANDARD_DIRECTION_LETTER
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isRecipientRespondent(AsylumCase asylumCase) {
        return directionFinder
            .findFirst(asylumCase, DirectionTag.NONE)
            .map(direction -> direction.getParties().equals(Parties.RESPONDENT))
            .orElse(false);
    }
}
