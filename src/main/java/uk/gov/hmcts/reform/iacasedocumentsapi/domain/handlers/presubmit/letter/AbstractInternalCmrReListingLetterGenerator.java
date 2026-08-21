package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

import java.util.Objects;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_NOTIFICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.CMR_RE_LISTING;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isRemoteCmrHearing;

/**
 * Common behaviour for the letters generated when a case management review hearing is re-listed.
 * Subclasses only supply the case condition that makes them applicable and the recipient-specific
 * document tag/creator.
 */
public abstract class AbstractInternalCmrReListingLetterGenerator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> documentCreator;
    private final DocumentHandler documentHandler;
    private final DocumentTag documentTag;

    protected AbstractInternalCmrReListingLetterGenerator(
        DocumentCreator<AsylumCase> documentCreator,
        DocumentHandler documentHandler,
        DocumentTag documentTag
    ) {
        this.documentCreator = documentCreator;
        this.documentHandler = documentHandler;
        this.documentTag = documentTag;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        Objects.requireNonNull(callbackStage, "callbackStage must not be null");
        Objects.requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == CMR_RE_LISTING
                && !isRemoteCmrHearing(asylumCase)
               && isApplicable(asylumCase);
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
        final CaseDetails<AsylumCase> caseDetailsBefore = callback.getCaseDetailsBefore()
            .orElseThrow(() -> new IllegalStateException("previous case data is not present"));

        Document letter = documentCreator.create(caseDetails, caseDetailsBefore);

        documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase,
            letter,
            LETTER_NOTIFICATION_DOCUMENTS,
            documentTag
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    protected abstract boolean isApplicable(AsylumCase asylumCase);
}
