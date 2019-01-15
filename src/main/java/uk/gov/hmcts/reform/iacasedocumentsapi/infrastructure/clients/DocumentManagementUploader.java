package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.util.Collections;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentUploader;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.UserCredentialsProvider;

@Service
public class DocumentManagementUploader implements DocumentUploader {

    private final DocumentUploadClientApi documentUploadClientApi;
    private final AuthTokenGenerator serviceAuthorizationTokenGenerator;
    private final UserCredentialsProvider userCredentialsProvider;

    public DocumentManagementUploader(
        DocumentUploadClientApi documentUploadClientApi,
        AuthTokenGenerator serviceAuthorizationTokenGenerator,
        UserCredentialsProvider userCredentialsProvider
    ) {
        this.documentUploadClientApi = documentUploadClientApi;
        this.serviceAuthorizationTokenGenerator = serviceAuthorizationTokenGenerator;
        this.userCredentialsProvider = userCredentialsProvider;
    }

    public Document upload(
        Resource resource,
        String contentType
    ) {
        final String serviceAuthorizationToken = serviceAuthorizationTokenGenerator.generate();
        final String accessToken = userCredentialsProvider.getAccessToken();
        final String userId = userCredentialsProvider.getId();

        try {

            MultipartFile file = new InMemoryMultipartFile(
                resource.getFilename(),
                resource.getFilename(),
                contentType,
                ByteStreams.toByteArray(resource.getInputStream())
            );

            UploadResponse uploadResponse =
                documentUploadClientApi
                    .upload(
                        accessToken,
                        serviceAuthorizationToken,
                        userId,
                        Collections.singletonList(file)
                    );

            uk.gov.hmcts.reform.document.domain.Document uploadedDocument =
                uploadResponse
                    .getEmbedded()
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
                    .originalDocumentName
            );

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
