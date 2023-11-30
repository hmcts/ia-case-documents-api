package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentUploader;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;

@Service
@RequiredArgsConstructor
public class DocumentManagementUploader implements DocumentUploader {

    private final FeatureToggler featureToggler;
    private final DmDocumentManagementUploader dmDocumentManagementUploader;
    private final CdamDocumentManagementUploader cdamDocumentManagementUploader;

    @Override
    public Document upload(
            Resource resource,
            String classification,
            String caseTypeId,
            String jurisdictionId,
            String contentType
    ) {
        if (featureToggler.getValue("use-ccd-document-am", false)) {
            return cdamDocumentManagementUploader.upload(
                    resource,
                    classification,
                    caseTypeId,
                    jurisdictionId,
                    contentType
            );
        } else {
            return dmDocumentManagementUploader.upload(resource, contentType);
        }
    }
}
