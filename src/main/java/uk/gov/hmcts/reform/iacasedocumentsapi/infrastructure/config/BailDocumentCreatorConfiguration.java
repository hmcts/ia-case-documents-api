package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.BailCaseFileNameQualifier;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentGenerator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentUploader;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail.BailSubmissionTemplate;

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
}
