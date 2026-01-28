package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
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

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_BUNDLE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.OTHER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.LIST_CASE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;

@Slf4j
@Component
public class InternalCaseListedAppellantLetterBundler implements PreSubmitCallbackHandler<AsylumCase> {

    private final String fileExtension;
    private final String fileName;
    private final boolean isEmStitchingEnabled;
    private final FileNameQualifier<AsylumCase> fileNameQualifier;
    private final DocumentBundler documentBundler;
    private final DocumentHandler documentHandler;

    public InternalCaseListedAppellantLetterBundler(
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
            && (letterNeedsBundlingForAppellant(asylumCase) || hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase))
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

        boolean needsLegalRepBundle = hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase);
        boolean needsAppellantBundle = letterNeedsBundlingForAppellant(asylumCase);

        log.info("InternalCaseListedAppellantLetterBundler: Starting handle, needsLegalRepBundle={}, needsAppellantBundle={}",
            needsLegalRepBundle, needsAppellantBundle);

        // Start bundling operations in parallel
        CompletableFuture<Document> legalRepBundleFuture = null;
        CompletableFuture<Document> appellantBundleFuture = null;

        if (needsLegalRepBundle) {
            final String qualifiedDocumentFileName = fileNameQualifier.get(fileName + "." + fileExtension, caseDetails);
            List<DocumentWithMetadata> bundleDocuments = getMaybeLetterNotificationDocuments(asylumCase, DocumentTag.INTERNAL_CASE_LISTED_LR_LETTER);

            log.info("InternalCaseListedAppellantLetterBundler: Starting async legal rep bundle creation");
            legalRepBundleFuture = CompletableFuture.supplyAsync(() ->
                documentBundler.bundleWithoutContentsOrCoverSheets(
                    bundleDocuments,
                    "Letter bundle documents",
                    qualifiedDocumentFileName
                )
            );
        }

        if (needsAppellantBundle) {
            final String qualifiedDocumentFileName = fileNameQualifier.get(fileName + "." + fileExtension, caseDetails);
            List<DocumentWithMetadata> bundleDocuments = getMaybeLetterNotificationDocuments(asylumCase, DocumentTag.INTERNAL_CASE_LISTED_LETTER);

            log.info("InternalCaseListedAppellantLetterBundler: Starting async appellant bundle creation");
            appellantBundleFuture = CompletableFuture.supplyAsync(() ->
                documentBundler.bundleWithoutContentsOrCoverSheets(
                    bundleDocuments,
                    "Letter bundle documents",
                    qualifiedDocumentFileName
                )
            );
        }

        // Wait for both operations to complete and add documents to case
        try {
            if (legalRepBundleFuture != null) {
                Document internalCaseListedLetterBundle = legalRepBundleFuture.get();
                log.info("InternalCaseListedAppellantLetterBundler: Legal rep bundle completed, adding to case");
                documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                    asylumCase,
                    internalCaseListedLetterBundle,
                    LETTER_BUNDLE_DOCUMENTS,
                    DocumentTag.INTERNAL_CASE_LISTED_LR_LETTER_BUNDLE
                );
            }

            if (appellantBundleFuture != null) {
                Document internalCaseListedLetterBundle = appellantBundleFuture.get();
                log.info("InternalCaseListedAppellantLetterBundler: Appellant bundle completed, adding to case");
                documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                    asylumCase,
                    internalCaseListedLetterBundle,
                    LETTER_BUNDLE_DOCUMENTS,
                    DocumentTag.INTERNAL_CASE_LISTED_LETTER_BUNDLE
                );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Bundle creation was interrupted", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Bundle creation failed", e.getCause());
        }

        log.info("InternalCaseListedAppellantLetterBundler: Handle completed successfully");
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private static boolean letterNeedsBundlingForAppellant(AsylumCase asylumCase) {
        return (isInternalCase(asylumCase) && !isAppellantInDetention(asylumCase))
            || isDetainedInFacilityType(asylumCase, OTHER);
    }
}
