package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DIRECTION_EDIT_PARTIES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAppellantInDetention;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isInternalCase;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
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

@Component
public class InternalChangeDirectionDueDateLetterGenerator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> documentCreator;
    private final DocumentHandler documentHandler;

    public InternalChangeDirectionDueDateLetterGenerator(
        @Qualifier("internalChangeDueDateLetter") DocumentCreator<AsylumCase> documentCreator,
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

        Event event = callback.getEvent();

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && Event.CHANGE_DIRECTION_DUE_DATE == event
               && isInternalCase(asylumCase)
               && isAppellantInDetention(asylumCase)
               && isDirectionPartyAppellantOrAppellantRespondent(asylumCase);
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

        Document internalChangeDirectionDueDateLetter = documentCreator.create(caseDetails);

        documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase,
            internalChangeDirectionDueDateLetter,
            NOTIFICATION_ATTACHMENT_DOCUMENTS,
            DocumentTag.INTERNAL_CHANGE_DIRECTION_DUE_DATE_LETTER
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isDirectionPartyAppellantOrAppellantRespondent(AsylumCase asylumCase) {
        return asylumCase.read(DIRECTION_EDIT_PARTIES, Parties.class)
                .map(Parties -> Parties.equals(Parties.APPELLANT) || Parties.equals(Parties.APPELLANT_AND_RESPONDENT))
                .orElse(false);
    }
}
