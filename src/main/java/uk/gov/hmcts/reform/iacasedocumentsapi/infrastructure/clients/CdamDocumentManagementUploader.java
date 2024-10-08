package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import com.google.common.io.ByteStreams;
import java.util.Collections;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;

/**
 * This class supersedes DMDocumentManagementUploader. Its usage is driven by a feature flag.
 */
@Component
@ComponentScan("uk.gov.hmcts.reform.ccd.document.am.feign")
@Slf4j
public class CdamDocumentManagementUploader {

    private final CaseDocumentClient caseDocumentClient;
    private final AuthTokenGenerator serviceAuthorizationTokenGenerator;
    private final UserDetailsProvider userDetailsProvider;

    public CdamDocumentManagementUploader(
        CaseDocumentClient caseDocumentClient,
        AuthTokenGenerator serviceAuthorizationTokenGenerator,
        @Qualifier("requestUser") UserDetailsProvider userDetailsProvider
    ) {
        this.caseDocumentClient = caseDocumentClient;
        this.serviceAuthorizationTokenGenerator = serviceAuthorizationTokenGenerator;
        this.userDetailsProvider = userDetailsProvider;
    }

    @SneakyThrows
    public Document upload(Resource resource, String contentType) {
        final String serviceAuthorizationToken = serviceAuthorizationTokenGenerator.generate();
        final UserDetails userDetails = userDetailsProvider.getUserDetails();
        final String accessToken = userDetails.getAccessToken();

        MultipartFile file = new InMemoryMultipartFile(
            resource.getFilename(),
            resource.getFilename(),
            contentType,
            ByteStreams.toByteArray(resource.getInputStream())
        );

        log.info("Uploading {} using CaseDocumentClient (no CDAM)", resource.getFilename());
        UploadResponse uploadResponse = caseDocumentClient.uploadDocuments(
            accessToken,
            serviceAuthorizationToken,
            "Asylum",
            "IA",
            Collections.singletonList(file)
        );

        uk.gov.hmcts.reform.ccd.document.am.model.Document uploadedDocument = uploadResponse.getDocuments()
                                                                                  .stream().findFirst().orElseThrow(() ->
                                                                                                                        new DocumentServiceResponseException("Document cannot be uploaded, please try again"));

        return new Document(
            uploadedDocument.links.self.href,
            uploadedDocument.links.binary.href,
            uploadedDocument.originalDocumentName
        );
    }
}

