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

    private final DmSystemDocumentManagementUploader dmSystemDocumentManagementUploader;

    public Document upload(
            Resource resource,
            String classification,
            String caseTypeId,
            String jurisdictionId,
            String contentType
    ) {
        if (featureToggler.getValue("use-ccd-document-am", false)) {
            return cdamSystemDocumentManagementUploader.upload(
                    resource,
                    classification,
                    caseTypeId,
                    jurisdictionId,
                    contentType
            );
        } else {
            return dmSystemDocumentManagementUploader.upload(resource, contentType);
        }
    }
}
