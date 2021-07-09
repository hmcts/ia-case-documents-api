package uk.gov.hmcts.reform.iacasedocumentsapi.util;

import java.io.IOException;
import java.util.List;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;

@Service
public class SystemDocumentManagementUploader {

    private final CaseDocumentClient caseDocumentClient;
    private final AuthorizationHeadersProvider authorizationHeadersProvider;

    public SystemDocumentManagementUploader(
            CaseDocumentClient caseDocumentClient,
        AuthorizationHeadersProvider authorizationHeadersProvider
    ) {
        this.caseDocumentClient = caseDocumentClient;
        this.authorizationHeadersProvider = authorizationHeadersProvider;
    }

    public Document upload(
        Resource resource,
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

        final String userId = "1";

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
