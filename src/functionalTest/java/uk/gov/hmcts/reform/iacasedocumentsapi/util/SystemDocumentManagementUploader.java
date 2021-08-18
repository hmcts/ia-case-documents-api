package uk.gov.hmcts.reform.iacasedocumentsapi.util;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.CaseDocumentsMetadata;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentHashToken;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.utils.InMemoryMultipartFile;

import static com.google.common.collect.Lists.newArrayList;

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
                .getCaseOfficerAuthorization()
                .getValue("ServiceAuthorization");

        final String accessToken =
            authorizationHeadersProvider
                .getCaseOfficerAuthorization()
                .getValue("Authorization");

        final String userId = "1";

        try {

            MultipartFile file = new InMemoryMultipartFile(
                resource.getFilename(),
                resource.getFilename(),
                contentType,
                ByteStreams.toByteArray(resource.getInputStream())
            );

            // 1629280295439399
            UploadResponse uploadResponse =
                    caseDocumentClient
                            .uploadDocuments(
                                accessToken,
                                serviceAuthorizationToken,
                                "Asylum",
                                "IA",
                                Collections.singletonList(file)
                            );

            uk.gov.hmcts.reform.ccd.document.am.model.Document uploadedDocument =
                uploadResponse
                    .getDocuments()
                    .get(0);

            caseDocumentClient
                .patchDocument(
                    accessToken,
                    serviceAuthorizationToken,
                    CaseDocumentsMetadata.builder()
                        .caseId("1629280295439399")
                        .caseTypeId("Asylum")
                        .jurisdictionId("IA")
                        .documentHashTokens(newArrayList(
                            DocumentHashToken.builder().hashToken(uploadedDocument.hashToken).id(UUID.randomUUID().toString()).build())
                        )
                        .build()
                );

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
