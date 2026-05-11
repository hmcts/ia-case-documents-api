package uk.gov.hmcts.reform.iacasedocumentsapi.util;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;

@Service
@RequiredArgsConstructor
public class SystemDocumentManagementUploader {

    private final CdamSystemDocumentManagementUploader cdamSystemDocumentManagementUploader;

    public Document upload(Resource resource, String contentType) {
        return cdamSystemDocumentManagementUploader.upload(resource, contentType);
    }
}
