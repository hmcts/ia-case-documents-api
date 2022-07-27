package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.*;


@Configuration
public class CaseDocumentClientConfiguration {

    @Bean
    @Primary
    public CaseDocumentClient caseDocumentClient(
            @Value("${ccdGatewayUrl}") final String ccdGatewayUrl
    ) {
        return new CaseDocumentClient(new CaseDocumentClientApi() {
            @Override
            public UploadResponse uploadDocuments(String authorisation, String serviceAuth, DocumentUploadRequest uploadRequest) {
                return null;
            }

            @Override
            public ResponseEntity<Resource> getDocumentBinary(String authorisation, String serviceAuth, UUID documentId) {
                return null;
            }

            @Override
            public Document getMetadataForDocument(String authorisation, String serviceAuth, UUID documentId) {
                return null;
            }

            @Override
            public void deleteDocument(String authorisation, String serviceAuth, UUID documentId, boolean permanent) {

            }

            @Override
            public DocumentTTLResponse patchDocument(String authorisation, String serviceAuth, UUID documentId, DocumentTTLRequest ttl) {
                return null;
            }

            @Override
            public PatchDocumentMetaDataResponse patchDocument(String authorisation, String serviceAuth, CaseDocumentsMetadata caseDocumentsMetadata) {
                return null;
            }
        });
    }
}