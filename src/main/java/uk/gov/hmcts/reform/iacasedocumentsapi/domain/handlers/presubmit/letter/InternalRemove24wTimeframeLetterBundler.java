package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentBundler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FileNameQualifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_BUNDLE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_NOTIFICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getLatestLetterNotificationDocument;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getMaybeLetterNotificationDocuments;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.hasBeenSubmittedAsLegalRepresentedInternalCase;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isInternalCase;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.hasCompleteCaseReviewDate;

@Component
public class InternalRemove24wTimeframeLetterBundler implements PreSubmitCallbackHandler<AsylumCase> {

    private final String fileExtension;
    private final String fileName;
    private final boolean isEmStitchingEnabled;
    private final FileNameQualifier<AsylumCase> fileNameQualifier;
    private final DocumentBundler documentBundler;
    private final DocumentHandler documentHandler;

    public InternalRemove24wTimeframeLetterBundler(
        @Value("${internalRemove24wTimeframeLetterWithAttachment.fileExtension}") String fileExtension,
        @Value("${internalRemove24wTimeframeLetterWithAttachment.fileName}") String fileName,
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

        return callback.getEvent() == Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS
            && isInternalCase(asylumCase)
            && hasCompleteCaseReviewDate(asylumCase)
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
        List<DocumentWithMetadata> bundleDocuments = new ArrayList<>();
        bundleDocuments.addAll(getLatestLetterNotificationDocument(asylumCase, DocumentTag.STF_24WEEKS_REMOVAL_DECISION_LETTER));
        bundleDocuments.addAll(getLatestLetterNotificationDocument(asylumCase, DocumentTag.STF_24WEEKS_REMOVAL_DECISION_DOCUMENT));

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
        if (hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase)) {
            List<DocumentWithMetadata> bundleDocumentsLR = new ArrayList<>();
            bundleDocumentsLR.addAll(getLatestLetterNotificationDocument(asylumCase, DocumentTag.STF_24WEEKS_REMOVAL_DECISION_LETTER_LR));
            bundleDocumentsLR.addAll(getLatestLetterNotificationDocument(asylumCase, DocumentTag.STF_24WEEKS_REMOVAL_DECISION_DOCUMENT));

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

            documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                asylumCase,
                legalRepLrBundleFuture.join(),
                LETTER_BUNDLE_DOCUMENTS,
                DocumentTag.STF_24WEEKS_REMOVAL_DECISION_LETTER_LR_BUNDLE
            );
        } else {
            CompletableFuture.allOf(appellantLrBundleFuture).join();
        }

        documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase,
            appellantLrBundleFuture.join(),
            LETTER_BUNDLE_DOCUMENTS,
            DocumentTag.STF_24WEEKS_REMOVAL_DECISION_LETTER_BUNDLE
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
