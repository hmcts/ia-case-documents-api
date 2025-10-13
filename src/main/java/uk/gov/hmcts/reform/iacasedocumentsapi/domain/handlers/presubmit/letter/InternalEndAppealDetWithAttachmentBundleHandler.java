//package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
//import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
//import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
//import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
//import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
//import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority;
//import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
//import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
//import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
//import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
//import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentBundler;
//import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
//import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FileNameQualifier;
//
//import java.util.List;
//
//import static java.util.Objects.requireNonNull;
//import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
//import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.*;
//import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.END_APPEAL;
//import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;
//import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getMaybeLetterNotificationDocuments;
//
//@Component
//public class InternalEndAppealDetWithAttachmentBundleHandler implements PreSubmitCallbackHandler<AsylumCase> {
//
//    private final String fileExtension;
//    private final String fileName;
//    private final boolean isEmStitchingEnabled;
//    private final FileNameQualifier<AsylumCase> fileNameQualifier;
//    private final DocumentBundler documentBundler;
//    private final DocumentHandler documentHandler;
//
//    public InternalEndAppealDetWithAttachmentBundleHandler(
//            @Value("${internalEndAppealDetWithAttachment.fileExtension}") String fileExtension,
//            @Value("${internalEndAppealDetWithAttachment.fileName}") String fileName,
//            @Value("${featureFlag.isEmStitchingEnabled}") boolean isEmStitchingEnabled,
//            FileNameQualifier<AsylumCase> fileNameQualifier,
//            DocumentBundler documentBundler,
//            DocumentHandler documentHandler
//    ) {
//        this.fileExtension = fileExtension;
//        this.fileName = fileName;
//        this.isEmStitchingEnabled = isEmStitchingEnabled;
//        this.fileNameQualifier = fileNameQualifier;
//        this.documentBundler = documentBundler;
//        this.documentHandler = documentHandler;
//    }
//
//    public boolean canHandle(
//            PreSubmitCallbackStage callbackStage,
//            Callback<AsylumCase> callback
//    ) {
//        requireNonNull(callbackStage, "callbackStage must not be null");
//        requireNonNull(callback, "callback must not be null");
//
//        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
//
//        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
//                && callback.getEvent() == END_APPEAL
//                && isInternalCase(asylumCase)
//                && hasAppealBeenSubmittedByAppellantInternalCase(asylumCase)
//                && isAppellantInDetention(asylumCase)
//                && isDetainedInOneOfFacilityTypes(asylumCase, IRC, PRISON)
//                && isEmStitchingEnabled;
//    }
//
//    @Override
//    public DispatchPriority getDispatchPriority() {
//        return DispatchPriority.LATE;
//    }
//
//    public PreSubmitCallbackResponse<AsylumCase> handle(
//            PreSubmitCallbackStage callbackStage,
//            Callback<AsylumCase> callback
//    ) {
//        if (!canHandle(callbackStage, callback)) {
//            throw new IllegalStateException("Cannot handle callback");
//        }
//
//        final CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
//        final AsylumCase asylumCase = caseDetails.getCaseData();
//
//        List<DocumentWithMetadata> bundleDocuments = getMaybeLetterNotificationDocuments(asylumCase, DocumentTag.END_APPEAL);
//
//        final String qualifiedDocumentFileName = fileNameQualifier.get(fileName + "." + fileExtension, caseDetails);
//
//        Document internalEndAppealLetterBundle = documentBundler.bundleWithoutContentsOrCoverSheets(
//                bundleDocuments,
//                "Letter bundle documents",
//                qualifiedDocumentFileName
//        );
//
//        documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
//                asylumCase,
//                internalEndAppealLetterBundle,
//                NOTIFICATION_ATTACHMENT_DOCUMENTS,
//                DocumentTag.END_APPEAL
//        );
//
//        return new PreSubmitCallbackResponse<>(asylumCase);
//    }
//}