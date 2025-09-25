package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_BUNDLE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_NOTIFICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.OTHER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentBundler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FileNameQualifier;

@Component
public class InternalRecordOutOfTimeDecisionLetterHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final String fileExtension;
    private final String fileName;
    private final boolean isEmStitchingEnabled;
    private final FileNameQualifier<AsylumCase> fileNameQualifier;
    private final DocumentBundler documentBundler;
    private final DocumentHandler documentHandler;

    public InternalRecordOutOfTimeDecisionLetterHandler(
        @Value("${internalOutOfTimeDecisionLetterWithAttachment.fileExtension}") String fileExtension,
        @Value("${internalOutOfTimeDecisionLetterWithAttachment.fileName}") String fileName,
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

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.RECORD_OUT_OF_TIME_DECISION
               && isInternalCase(asylumCase)
               && (!isAppellantInDetention(asylumCase)
                || (hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase))
                || (hasBeenSubmittedByAppellantInternalCase(asylumCase)
                && isDetainedInFacilityType(asylumCase, OTHER)))
               && isEmStitchingEnabled;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATE;
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

        Optional<List<IdValue<DocumentWithMetadata>>> maybeLetterNotificationDocuments = asylumCase.read(LETTER_NOTIFICATION_DOCUMENTS);
        List<DocumentWithMetadata> bundleDocuments = maybeLetterNotificationDocuments
            .orElse(Collections.emptyList())
            .stream()
            .map(IdValue::getValue)
            .filter(document -> document.getTag() == DocumentTag.INTERNAL_OUT_OF_TIME_DECISION_LETTER)
            .collect(Collectors.toList());

        Document internalCaseListedLetterBundle = documentBundler.bundleWithoutContentsOrCoverSheets(
            bundleDocuments,
            "Letter bundle documents",
            qualifiedDocumentFileName
        );

        documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase,
            internalCaseListedLetterBundle,
            LETTER_BUNDLE_DOCUMENTS,
            DocumentTag.INTERNAL_OUT_OF_TIME_DECISION_LETTER_BUNDLE
        );

        asylumCase.clear(LETTER_NOTIFICATION_DOCUMENTS);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
