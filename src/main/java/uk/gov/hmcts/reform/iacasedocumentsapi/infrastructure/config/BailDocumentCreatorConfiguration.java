package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.BailCaseFileNameQualifier;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentGenerator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentUploader;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail.*;

@Configuration
public class BailDocumentCreatorConfiguration {

    @Bean("bailSubmission")
    public DocumentCreator<BailCase> getBailSubmissionDocumentCreator(
        @Value("${bailSubmissionDocument.contentType}") String contentType,
        @Value("${bailSubmissionDocument.fileExtension}") String fileExtension,
        @Value("${bailSubmissionDocument.fileName}") String fileName,
        BailCaseFileNameQualifier fileNameQualifier,
        BailSubmissionTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<BailCase>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }
    
    @Bean("bailSubmissionWithUt")
    public DocumentCreator<BailCase> getBailSubmissionWithUtDocumentCreator(
        @Value("${bailSubmissionDocument.contentType}") String contentType,
        @Value("${bailSubmissionDocument.fileExtension}") String fileExtension,
        @Value("${bailSubmissionDocument.fileName}") String fileName,
        BailCaseFileNameQualifier fileNameQualifier,
        BailSubmissionUtTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<BailCase>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }


    @Bean("decisionUnsignedGranted")
    public DocumentCreator<BailCase> getBailDecisionUnsignedGrantedDocumentCreator(
            @Value("${decisionUnsignedDocument.contentType}") String contentType,
            @Value("${decisionUnsignedDocument.fileExtension}") String fileExtension,
            @Value("${decisionUnsignedDocument.fileName}") String fileName,
            BailCaseFileNameQualifier fileNameQualifier,
            BailDecisionUnsignedGrantedTemplate documentTemplate,
            DocumentGenerator documentGenerator,
            DocumentUploader documentUploader
    ) {
        return new DocumentCreator<BailCase>(
                contentType,
                fileExtension,
                fileName,
                fileNameQualifier,
                documentTemplate,
                documentGenerator,
                documentUploader
        );
    }

    @Bean("decisionUnsignedMindedRefusal")
    public DocumentCreator<BailCase> getBailDecisionUnsignedMindedRefusalDocumentCreator(
            @Value("${decisionUnsignedDocument.contentType}") String contentType,
            @Value("${decisionUnsignedDocument.fileExtension}") String fileExtension,
            @Value("${decisionUnsignedDocument.fileName}") String fileName,
            BailCaseFileNameQualifier fileNameQualifier,
            BailDecisionUnsignedMindedRefusalTemplate documentTemplate,
            DocumentGenerator documentGenerator,
            DocumentUploader documentUploader
    ) {
        return new DocumentCreator<BailCase>(
                contentType,
                fileExtension,
                fileName,
                fileNameQualifier,
                documentTemplate,
                documentGenerator,
                documentUploader
        );
    }

    @Bean("decisionUnsignedRefusal")
    public DocumentCreator<BailCase> getBailDecisionUnsignedRefusalDocumentCreator(
            @Value("${decisionUnsignedDocument.contentType}") String contentType,
            @Value("${decisionUnsignedDocument.fileExtension}") String fileExtension,
            @Value("${decisionUnsignedDocument.fileName}") String fileName,
            BailCaseFileNameQualifier fileNameQualifier,
            BailDecisionUnsignedRefusalTemplate documentTemplate,
            DocumentGenerator documentGenerator,
            DocumentUploader documentUploader
    ) {
        return new DocumentCreator<BailCase>(
                contentType,
                fileExtension,
                fileName,
                fileNameQualifier,
                documentTemplate,
                documentGenerator,
                documentUploader
        );
    }

    @Bean("bailEndApplication")
    public DocumentCreator<BailCase> getBailEndApplicationDocumentCreator(
        @Value("${bailEndApplication.contentType}") String contentType,
        @Value("${bailEndApplication.fileExtension}") String fileExtension,
        @Value("${bailEndApplication.fileName}") String fileName,
        BailCaseFileNameQualifier fileNameQualifier,
        BailEndApplicationTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<BailCase>(
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
