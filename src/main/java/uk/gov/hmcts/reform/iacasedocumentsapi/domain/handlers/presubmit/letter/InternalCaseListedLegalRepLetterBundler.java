package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_BUNDLE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.LIST_CASE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getMaybeLetterNotificationDocuments;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.hasBeenSubmittedAsLegalRepresentedInternalCase;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentBundler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FileNameQualifier;

@Component
public class InternalCaseListedLegalRepLetterBundler implements PreSubmitCallbackHandler<AsylumCase> {

    private final String fileExtension;
    private final String fileName;
    private final boolean isEmStitchingEnabled;
    private final FileNameQualifier<AsylumCase> fileNameQualifier;
    private final DocumentBundler documentBundler;
    private final DocumentHandler documentHandler;

    public InternalCaseListedLegalRepLetterBundler(
        @Value("${internalCaseListedLetterWithAttachment.fileExtension}") String fileExtension,
        @Value("${internalCaseListedLetterWithAttachment.fileName}") String fileName,
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
               && callback.getEvent() == LIST_CASE
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

        final String qualifiedDocumentFileName = fileNameQualifier.get(fileName + "." + fileExtension, caseDetails);

        List<DocumentWithMetadata> bundleDocuments = getMaybeLetterNotificationDocuments(asylumCase, DocumentTag.INTERNAL_CASE_LISTED_LETTER);
        CompletableFuture<Document> appellantLrBundleFuture = CompletableFuture.supplyAsync(() -> documentBundler.bundleWithoutContentsOrCoverSheets(
                bundleDocuments,
                "Letter bundle documents",
                qualifiedDocumentFileName
        ));

        List<DocumentWithMetadata> bundleDocumentsLR = getMaybeLetterNotificationDocuments(asylumCase, DocumentTag.INTERNAL_CASE_LISTED_LR_LETTER);
        CompletableFuture<Document> legalRepLrBundleFuture = CompletableFuture.supplyAsync(() -> documentBundler.bundleWithoutContentsOrCoverSheets(
                bundleDocumentsLR,
                "Letter bundle documents",
                qualifiedDocumentFileName
        ));

        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(appellantLrBundleFuture, legalRepLrBundleFuture);

        combinedFuture.thenRun(() -> {
            documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                    asylumCase,
                    appellantLrBundleFuture.join(),
                    LETTER_BUNDLE_DOCUMENTS,
                    DocumentTag.INTERNAL_CASE_LISTED_LETTER_BUNDLE
            );

            documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                    asylumCase,
                    legalRepLrBundleFuture.join(),
                    LETTER_BUNDLE_DOCUMENTS,
                    DocumentTag.INTERNAL_CASE_LISTED_LR_LETTER_BUNDLE
            );
        });

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
