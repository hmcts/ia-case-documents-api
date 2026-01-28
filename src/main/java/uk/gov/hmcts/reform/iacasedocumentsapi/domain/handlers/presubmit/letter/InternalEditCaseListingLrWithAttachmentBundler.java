package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentBundler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FileNameQualifier;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_BUNDLE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.EDIT_CASE_LISTING;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getMaybeLetterNotificationDocuments;

@Component
public class InternalEditCaseListingLrWithAttachmentBundler implements PreSubmitCallbackHandler<AsylumCase> {

    private final String fileExtension;
    private final String fileName;
    private final boolean isEmStitchingEnabled;
    private final FileNameQualifier<AsylumCase> fileNameQualifier;
    private final DocumentBundler documentBundler;
    private final DocumentHandler documentHandler;

    public InternalEditCaseListingLrWithAttachmentBundler(
            @Value("${internalEditCaseListingLetterWithAttachment.fileExtension}") String fileExtension,
            @Value("${internalEditCaseListingLetterWithAttachment.fileName}") String fileName,
            @Value("${featureFlag.isEmStitchingEnabled}") boolean isEmStitchingEnabled,
            FileNameQualifier<AsylumCase> fileNameQualifier,
            DocumentBundler documentBundler,
            DocumentHandler documentHandler
    ) {
        this.fileExtension = fileExtension;
        this.fileName = fileName;
        this.isEmStitchingEnabled = isEmStitchingEnabled;
        this.fileNameQualifier = fileNameQualifier;
        this.documentBundler = documentBundler;
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

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        final String qualifiedDocumentFileName = fileNameQualifier.get(fileName + "." + fileExtension, caseDetails);

        List<DocumentWithMetadata> bundleDocuments = getMaybeLetterNotificationDocuments(asylumCase, DocumentTag.INTERNAL_EDIT_CASE_LISTING_LETTER);
        CompletableFuture<Document> appellantLrBundleFuture = CompletableFuture.supplyAsync(() -> {
            try {
                RequestContextHolder.setRequestAttributes(requestAttributes);
                return documentBundler.bundleWithoutContentsOrCoverSheets(
                        bundleDocuments,
                        "Letter bundle documents",
                        qualifiedDocumentFileName
                );
            } finally {
                RequestContextHolder.resetRequestAttributes();
            }
        });

        List<DocumentWithMetadata> bundleDocumentsLR = getMaybeLetterNotificationDocuments(asylumCase, DocumentTag.INTERNAL_EDIT_CASE_LISTING_LR_LETTER);
        CompletableFuture<Document> legalRepLrBundleFuture = CompletableFuture.supplyAsync(() -> {
            try {
                RequestContextHolder.setRequestAttributes(requestAttributes);
                return documentBundler.bundleWithoutContentsOrCoverSheets(
                        bundleDocumentsLR,
                        "Letter bundle documents",
                        qualifiedDocumentFileName
                );
            } finally {
                RequestContextHolder.resetRequestAttributes();
            }
        });

        CompletableFuture.allOf(appellantLrBundleFuture, legalRepLrBundleFuture).join();

        if (appellantLrBundleFuture != null) {
            documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                    asylumCase,
                    appellantLrBundleFuture.join(),
                    LETTER_BUNDLE_DOCUMENTS,
                    DocumentTag.INTERNAL_EDIT_CASE_LISTING_LETTER_BUNDLE
            );
        }

        if (legalRepLrBundleFuture != null) {
            documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                    asylumCase,
                    legalRepLrBundleFuture.join(),
                    LETTER_BUNDLE_DOCUMENTS,
                    DocumentTag.INTERNAL_EDIT_CASE_LISTING_LR_LETTER_BUNDLE
            );
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}