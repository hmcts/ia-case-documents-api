package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_NOTIFICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.hasBeenSubmittedAsLegalRepresentedInternalCase;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isInternalCase;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.hasCompleteCaseReviewDate;

@Component
public class InternalRemove24wTimeframeLetterGenerator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> documentCreator;
    private final DocumentCreator<AsylumCase> lrDocumentCreator;
    private final DocumentHandler documentHandler;

    public InternalRemove24wTimeframeLetterGenerator(
        @Qualifier("internalRemove24wTimeframeLetter") DocumentCreator<AsylumCase> documentCreator,
        @Qualifier("internalRemove24wTimeframeLrLetter") DocumentCreator<AsylumCase> lrDocumentCreator,
        DocumentHandler documentHandler
    ) {
        this.documentCreator = documentCreator;
        this.lrDocumentCreator = lrDocumentCreator;
        this.documentHandler = documentHandler;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        return callback.getEvent() == Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS
            && hasCompleteCaseReviewDate(asylumCase)
            && isInternalCase(asylumCase);
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

        Document letter = documentCreator.create(caseDetails);
        documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase,
            letter,
            LETTER_NOTIFICATION_DOCUMENTS,
            DocumentTag.STF_24WEEKS_REMOVAL_DECISION_LETTER
        );

        if (hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase)) {
            Document lrLetter = lrDocumentCreator.create(caseDetails);
            documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                asylumCase,
                lrLetter,
                LETTER_NOTIFICATION_DOCUMENTS,
                DocumentTag.STF_24WEEKS_REMOVAL_DECISION_LETTER_LR
            );
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
