package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_BUNDLE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.EDIT_CASE_LISTING;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getMaybeLetterNotificationDocuments;

@Component
public class InternalEditCaseListingLrWithAttachmentBundler implements PreSubmitCallbackHandler<AsylumCase> {

    private final boolean isEmStitchingEnabled;
    private final DocumentHandler documentHandler;

    public InternalEditCaseListingLrWithAttachmentBundler(
            @Value("${featureFlag.isEmStitchingEnabled}") boolean isEmStitchingEnabled,
            DocumentHandler documentHandler
    ) {
        this.isEmStitchingEnabled = isEmStitchingEnabled;
        this.documentHandler = documentHandler;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATE;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == EDIT_CASE_LISTING
                && hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase)
                && isEmStitchingEnabled;
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

        List<DocumentWithMetadata> bundleDocuments = getMaybeLetterNotificationDocuments(asylumCase, DocumentTag.INTERNAL_EDIT_CASE_LISTING_LR_LETTER);


        documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                asylumCase,
                bundleDocuments.get(0).getDocument(),
                LETTER_BUNDLE_DOCUMENTS,
                DocumentTag.INTERNAL_EDIT_CASE_LISTING_LR_LETTER_BUNDLE
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}