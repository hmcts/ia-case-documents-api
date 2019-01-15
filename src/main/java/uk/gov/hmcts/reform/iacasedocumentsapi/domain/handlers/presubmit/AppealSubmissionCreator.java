package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentGenerator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentUploader;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.AppealSubmissionTemplate;

@Component
public class AppealSubmissionCreator implements PreSubmitCallbackHandler<AsylumCase> {

    private final String documentContentType;
    private final String documentFileExtension;
    private final AppealSubmissionTemplate appealSubmissionTemplate;
    private final DocumentGenerator documentGenerator;
    private final DocumentUploader documentUploader;
    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public AppealSubmissionCreator(
        @Value("${appealSubmissionDocument.contentType}") String documentContentType,
        @Value("${appealSubmissionDocument.fileExtension}") String documentFileExtension,
        AppealSubmissionTemplate appealSubmissionTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader,
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender
    ) {
        this.documentContentType = documentContentType;
        this.documentFileExtension = documentFileExtension;
        this.appealSubmissionTemplate = appealSubmissionTemplate;
        this.documentGenerator = documentGenerator;
        this.documentUploader = documentUploader;
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.SUBMIT_APPEAL;
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

        final String appealReferenceNumber =
            asylumCase
                .getAppealReferenceNumber()
                .orElseThrow(() -> new IllegalStateException("appealReferenceNumber is not present"));

        final String appellantFamilyName =
            asylumCase
                .getAppellantFamilyName()
                .orElseThrow(() -> new IllegalStateException("appellantFamilyName is not present"));

        final String fileName =
            appealReferenceNumber.replace("/", " ")
            + "-" + appellantFamilyName
            + "-" + "appeal-form";

        final String templateName = appealSubmissionTemplate.getName();
        final Map<String, Object> templateFieldValues = appealSubmissionTemplate.mapFieldValues(caseDetails);

        Resource documentResource = documentGenerator.generate(fileName, documentFileExtension, templateName, templateFieldValues);
        Document uploadedDocument = documentUploader.upload(documentResource, documentContentType);

        attachDocumentToCase(asylumCase, uploadedDocument);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void attachDocumentToCase(
        AsylumCase asylumCase,
        Document document
    ) {
        final List<IdValue<DocumentWithMetadata>> legalRepresentativeDocuments =
            asylumCase
                .getLegalRepresentativeDocuments()
                .orElse(Collections.emptyList());

        DocumentWithMetadata documentWithMetadata =
            documentReceiver.receive(
                document,
                "",
                DocumentTag.APPEAL_SUBMISSION
            );

        List<IdValue<DocumentWithMetadata>> allLegalRepresentativeDocuments =
            documentsAppender.append(
                legalRepresentativeDocuments,
                Collections.singletonList(documentWithMetadata),
                DocumentTag.APPEAL_SUBMISSION
            );

        asylumCase.setLegalRepresentativeDocuments(allLegalRepresentativeDocuments);
    }
}
