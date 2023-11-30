package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.util.Collections;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;

import static java.util.Collections.singletonList;

/**
 * This class supersedes DMDocumentManagementUploader. Its usage is driven by a feature flag.
 */
@Component
@ComponentScan("uk.gov.hmcts.reform.ccd.document.am.feign")
public class CdamDocumentManagementUploader {

    private final CaseDocumentClientApi caseDocumentClientApi;
    private final AuthTokenGenerator serviceAuthorizationTokenGenerator;
    private final UserDetailsProvider userDetailsProvider;

    public CdamDocumentManagementUploader(
            CaseDocumentClientApi caseDocumentClientApi,
            AuthTokenGenerator serviceAuthorizationTokenGenerator,
            @Qualifier("requestUser") UserDetailsProvider userDetailsProvider
    ) {
        this.caseDocumentClientApi = caseDocumentClientApi;
        this.serviceAuthorizationTokenGenerator = serviceAuthorizationTokenGenerator;
        this.userDetailsProvider = userDetailsProvider;
    }

    @SneakyThrows
    public Document upload(
            Resource resource,
            String classification,
            String caseTypeId,
            String jurisdictionId,
            String contentType
    ) {
        final String serviceAuthorizationToken = serviceAuthorizationTokenGenerator.generate();
        final UserDetails userDetails = userDetailsProvider.getUserDetails();
        final String accessToken = userDetails.getAccessToken();

        try {
            MultipartFile file = new InMemoryMultipartFile(
                resource.getFilename(),
                resource.getFilename(),
                contentType,
                ByteStreams.toByteArray(resource.getInputStream())
            );

            DocumentUploadRequest request = new DocumentUploadRequest(
                    classification,
                    caseTypeId,
                    jurisdictionId,
                    singletonList(file)
            );

            UploadResponse uploadResponse =
                    caseDocumentClientApi.uploadDocuments(
                                accessToken,
                                serviceAuthorizationToken,
                                request
                        );

        uk.gov.hmcts.reform.ccd.document.am.model.Document uploadedDocument = uploadResponse.getDocuments().get(0);

        return new Document(
                uploadedDocument.links.self.href,
                uploadedDocument.links.binary.href,
                uploadedDocument.originalDocumentName,
                uploadedDocument.hashToken
        );
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
