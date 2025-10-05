package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentBundler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FileNameQualifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.IRC;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.PRISON;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.RECORD_OUT_OF_TIME_DECISION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getDecisionOfNoticeDocuments;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isDetainedInOneOfFacilityTypes;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isInternalCase;

@Component
public class InternalDetainedOutOfTimeDecisionAllowedLetterGenerator implements PreSubmitCallbackHandler<AsylumCase> {

    private final String fileExtension;
    private final String fileName;
    private final DocumentCreator<AsylumCase> documentCreator;
    private final DocumentHandler documentHandler;
    private final DocumentBundler documentBundler;
    private final FileNameQualifier<AsylumCase> fileNameQualifier;

    public InternalDetainedOutOfTimeDecisionAllowedLetterGenerator(
            @Value("${internalDetainedOutOfTimeDecisionAllowedLetter.fileExtension}") String fileExtension,
            @Value("${internalDetainedOutOfTimeDecisionAllowedLetter.fileName}") String fileName,
            @Qualifier("internalDetainedOutOfTimeDecisionAllowedLetter") DocumentCreator<AsylumCase> documentCreator,
            DocumentHandler documentHandler,
            DocumentBundler documentBundler,
            FileNameQualifier<AsylumCase> fileNameQualifier
    ) {
        this.fileExtension = fileExtension;
        this.fileName = fileName;
        this.documentCreator = documentCreator;
        this.documentHandler = documentHandler;
        this.documentBundler = documentBundler;
        this.fileNameQualifier = fileNameQualifier;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        Objects.requireNonNull(callbackStage, "callbackStage must not be null");
        Objects.requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == RECORD_OUT_OF_TIME_DECISION
                && isInternalCase(asylumCase)
                && isDetainedInOneOfFacilityTypes(asylumCase, IRC, PRISON);
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

        Document internalDetainedOutOfTimeDecisionAllowedLetter = documentCreator.create(caseDetails);

        // Create bundle documents list
        List<DocumentWithMetadata> bundleDocuments = new ArrayList<>();

        // Add the generated letter to bundle
        DocumentWithMetadata letterDocument = new DocumentWithMetadata(
                internalDetainedOutOfTimeDecisionAllowedLetter,
                "Letter",
                "Letter",
                DocumentTag.INTERNAL_DETAINED_OUT_OF_TIME_DECISION_ALLOWED_LETTER
        );
        bundleDocuments.add(letterDocument);

        // Get decision document
        Document outOfTimeDecisionDocument = getDecisionOfNoticeDocuments(asylumCase);
        DocumentWithMetadata outOfTimeDecisionDocumentWithMetaData = new DocumentWithMetadata(
                outOfTimeDecisionDocument,
                "Letter",
                "Letter",
                DocumentTag.INTERNAL_DETAINED_OUT_OF_TIME_DECISION_ALLOWED_LETTER
        );
        bundleDocuments.add(outOfTimeDecisionDocumentWithMetaData);

        // Create qualified file name
        final String qualifiedDocumentFileName = fileNameQualifier.get(fileName + "." + fileExtension, caseDetails);

        // Create bundle
        Document internalDetainedOutOfTimeDecisionAllowedLetterBundle = documentBundler.bundleWithoutContentsOrCoverSheets(
                bundleDocuments,
                "Letter bundle documents",
                qualifiedDocumentFileName
        );

        // Add bundled document to notification attachment documents
        documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                asylumCase,
                internalDetainedOutOfTimeDecisionAllowedLetterBundle,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.INTERNAL_DETAINED_OUT_OF_TIME_DECISION_ALLOWED_LETTER
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
