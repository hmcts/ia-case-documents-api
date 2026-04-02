package uk.gov.hmcts.reform.iacasedocumentsapi.util;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;

@Service
@RequiredArgsConstructor
public class SystemDocumentManagementUploader {

    private final FeatureToggler featureToggler;

    private final CdamSystemDocumentManagementUploader cdamSystemDocumentManagementUploader;

    public Document upload(Resource resource, String contentType) {
        return cdamSystemDocumentManagementUploader.upload(resource, contentType);
    }
}
