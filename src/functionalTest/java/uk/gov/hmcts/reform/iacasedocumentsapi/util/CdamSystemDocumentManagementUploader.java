package uk.gov.hmcts.reform.iacasedocumentsapi.util;

import com.google.common.io.ByteStreams;
import lombok.SneakyThrows;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;

import static java.util.Collections.singletonList;

/**
 * This class supersedes DmDocumentManagementUploader. Its usage is driven by a feature flag.
 */
@Component
@ComponentScan("uk.gov.hmcts.reform.ccd.document.am.feign")
public class CdamSystemDocumentManagementUploader {
    private final CaseDocumentClient caseDocumentClient;
    private final AuthorizationHeadersProvider authorizationHeadersProvider;

    public CdamSystemDocumentManagementUploader(
        CaseDocumentClient caseDocumentClient,
        AuthorizationHeadersProvider authorizationHeadersProvider
    ) {
        this.caseDocumentClient = caseDocumentClient;
        this.authorizationHeadersProvider = authorizationHeadersProvider;
    }

    @SneakyThrows
    public Document upload(
            Resource resource,
            String classification,
            String caseTypeId,
            String jurisdictionId,
            String contentType
    ) {
        final String serviceAuthorizationToken = authorizationHeadersProvider
                                                     .getLegalRepresentativeAuthorization()
                                                     .getValue("ServiceAuthorization");

        final String accessToken = authorizationHeadersProvider
                                       .getLegalRepresentativeAuthorization()
                                       .getValue("Authorization");

        MultipartFile file = new InMemoryMultipartFile(
            resource.getFilename(),
            resource.getFilename(),
            contentType,
            ByteStreams.toByteArray(resource.getInputStream())
        );

        UploadResponse uploadResponse = caseDocumentClient.uploadDocuments(
            accessToken,
            serviceAuthorizationToken,
            "Asylum",
            "IA",
            singletonList(file)
        );

        uk.gov.hmcts.reform.ccd.document.am.model.Document uploadedDocument = uploadResponse.getDocuments().get(0);

        return new Document(
                uploadedDocument.links.self.href,
                uploadedDocument.links.binary.href,
                uploadedDocument.originalDocumentName,
                uploadedDocument.hashToken
        );
    }
}
/*
package uk.gov.hmcts.reform.iacasedocumentsapi.util;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.util.Collections;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;

@Service
public class SystemDocumentManagementUploader {

    private final CaseDocumentClientApi caseDocumentClientApi;
    private final AuthorizationHeadersProvider authorizationHeadersProvider;

    public SystemDocumentManagementUploader(
        CaseDocumentClientApi caseDocumentClientApi,
        AuthorizationHeadersProvider authorizationHeadersProvider
    ) {
        this.caseDocumentClientApi = caseDocumentClientApi;
        this.authorizationHeadersProvider = authorizationHeadersProvider;
    }

    public Document upload(
        Resource resource,
        String classification,
        String caseTypeId,
        String jurisdictionId,
        String contentType
    ) {
        final String serviceAuthorizationToken =
            authorizationHeadersProvider
                .getLegalRepresentativeAuthorization()
                .getValue("ServiceAuthorization");

        final String accessToken =
            authorizationHeadersProvider
                .getLegalRepresentativeAuthorization()
                .getValue("Authorization");

        try {

            MultipartFile file = new InMemoryMultipartFile(
                resource.getFilename(),
                resource.getFilename(),
                contentType,
                ByteStreams.toByteArray(resource.getInputStream())
            );

            DocumentUploadRequest request = new DocumentUploadRequest(classification,
                caseTypeId,jurisdictionId,Collections.singletonList(file));

            UploadResponse uploadResponse =
                caseDocumentClientApi
                    .uploadDocuments(
                        accessToken,
                        serviceAuthorizationToken,
                        request
                    );

            uk.gov.hmcts.reform.ccd.document.am.model.Document uploadedDocument =  uploadResponse
                .getDocuments()
                .get(0);

            return new Document(
                uploadedDocument
                    .links
                    .self
                    .href,
                uploadedDocument
                    .links
                    .binary
                    .href,
                uploadedDocument
                    .originalDocumentName,
                uploadedDocument
                    .hashToken
            );

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}

 */