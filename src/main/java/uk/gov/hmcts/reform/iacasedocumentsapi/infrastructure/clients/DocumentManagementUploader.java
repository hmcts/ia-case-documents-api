package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentUploader;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentManagementUploader implements DocumentUploader {

    private final CdamDocumentManagementUploader cdamDocumentManagementUploader;

    @Override
    public Document upload(Resource resource, String contentType) {
        log.info("Uploading {} using CDAM", resource.getFilename());
        return cdamDocumentManagementUploader.upload(resource, contentType);
    }
}
