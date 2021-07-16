package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import java.io.IOException;
import java.util.List;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentUploader;

@Service
public class DocumentManagementUploader implements DocumentUploader {

    private final CaseDocumentClient caseDocumentClient;
    private final AuthTokenGenerator serviceAuthorizationTokenGenerator;
    private final UserDetailsProvider userDetailsProvider;

    public DocumentManagementUploader(
            CaseDocumentClient caseDocumentClient,
        AuthTokenGenerator serviceAuthorizationTokenGenerator,
        @Qualifier("requestUser") UserDetailsProvider userDetailsProvider
    ) {
        this.caseDocumentClient = caseDocumentClient;
        this.serviceAuthorizationTokenGenerator = serviceAuthorizationTokenGenerator;
        this.userDetailsProvider = userDetailsProvider;
    }

    public Document upload(
        Resource resource,
        String contentType
    ) {
        final String serviceAuthorizationToken = serviceAuthorizationTokenGenerator.generate();
        final UserDetails userDetails = userDetailsProvider.getUserDetails();
        final String accessToken = userDetails.getAccessToken();

        try {

            DiskFileItem fileItem = new DiskFileItem(
                    resource.getFilename(),
                    "text/plain",
                    false,
                    resource.getFilename(),
                    (int) resource.getFile().length(),
                    resource.getFile().getParentFile());

            fileItem.getOutputStream();

            CommonsMultipartFile commonsMultipartFile = new CommonsMultipartFile(fileItem);

            UploadResponse uploadResponse =
                caseDocumentClient
                    .uploadDocuments(
                            serviceAuthorizationToken,
                            accessToken,
                            "ASYLUM",
                            "IA",
                            List.of(commonsMultipartFile)
                    );

            uk.gov.hmcts.reform.ccd.document.am.model.Document uploadedDocument =
                uploadResponse
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
