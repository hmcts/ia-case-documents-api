package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import lombok.extern.slf4j.Slf4j;
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
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.CMR_LISTING;

@Slf4j
public abstract class AbstractInternalCmrListingLetterGenerator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> documentCreator;
    private final DocumentHandler documentHandler;
    private final DocumentTag documentTag;

    protected AbstractInternalCmrListingLetterGenerator(
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
               && callback.getEvent() == CMR_LISTING
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

        Document letter = documentCreator.create(caseDetails);

        log.info("===================adding document field: {}, tag: {}", LETTER_NOTIFICATION_DOCUMENTS.value(), documentTag);
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
