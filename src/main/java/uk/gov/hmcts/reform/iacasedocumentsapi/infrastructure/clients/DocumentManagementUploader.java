package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentUploader;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentManagementUploader implements DocumentUploader {

    private final FeatureToggler featureToggler;
    private final DmDocumentManagementUploader dmDocumentManagementUploader;
    private final CdamDocumentManagementUploader cdamDocumentManagementUploader;

    @Override
    public Document upload(Resource resource, String contentType) {
        log.info("use-ccd-document-am is known: {}", featureToggler.isFlagKnown("use-ccd-document-am"));

        log.info("Uploading {} using CDAM", resource.getFilename());
        return cdamDocumentManagementUploader.upload(resource, contentType);
    }
}
