package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import org.springframework.core.io.Resource;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;

public interface DocumentUploader {

    Document upload(
            Resource resource,
            String classification,
            String caseTypeId,
            String jurisdictionId,
            String contentType
    );
}
