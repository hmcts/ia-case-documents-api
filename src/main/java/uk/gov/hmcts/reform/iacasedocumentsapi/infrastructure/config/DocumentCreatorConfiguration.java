package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.AsylumCaseFileNameQualifier;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentGenerator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentUploader;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.*;

@Configuration
public class DocumentCreatorConfiguration {

    @Bean("appealSubmission")
    public DocumentCreator<AsylumCase> getAppealSubmissionDocumentCreator(
        @Value("${appealSubmissionDocument.contentType}") String contentType,
        @Value("${appealSubmissionDocument.fileExtension}") String fileExtension,
        @Value("${appealSubmissionDocument.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        AppealSubmissionTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("hearingNotice")
    public DocumentCreator<AsylumCase> getHearingNoticeDocumentCreator(
        @Value("${hearingNoticeDocument.contentType}") String contentType,
        @Value("${hearingNoticeDocument.fileExtension}") String fileExtension,
        @Value("${hearingNoticeDocument.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        HearingNoticeTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("decisionAndReasons")
    public DocumentCreator<AsylumCase> getAndDecisionAndReasonsDocumentCreator(
            @Value("${decisionAndReasons.contentType}") String contentType,
            @Value("${decisionAndReasons.fileExtension}") String fileExtension,
            @Value("${decisionAndReasons.fileName}") String fileName,
            AsylumCaseFileNameQualifier fileNameQualifier,
            DecisionAndReasonsTemplate documentTemplate,
            DocumentGenerator documentGenerator,
            DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
                contentType,
                fileExtension,
                fileName,
                fileNameQualifier,
                documentTemplate,
                documentGenerator,
                documentUploader
        );
    }

    @Bean("decisionAndReasonsCoverLetter")
    public DocumentCreator<AsylumCase> getAndDecisionAndReasonsCoverLetterDocumentCreator(
        @Value("${decisionAndReasonsCoverLetter.contentType}") String contentType,
        @Value("${decisionAndReasonsCoverLetter.fileExtension}") String fileExtension,
        @Value("${decisionAndReasonsCoverLetter.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        DecisionAndReasonsCoverLetterTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("endAppealNotice")
    public DocumentCreator<AsylumCase> getEndAppealDocumentCreator(
        @Value("${endAppeal.contentType}") String contentType,
        @Value("${endAppeal.fileExtension}") String fileExtension,
        @Value("${endAppeal.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        EndAppealTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

}
