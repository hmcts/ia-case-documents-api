package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class DocumentManagementUploaderTest {

    @Mock private CcdCaseDocumentAmClient documentUploadClientApi;
    @Mock private AuthTokenGenerator serviceAuthorizationTokenGenerator;
    @Mock private UserDetailsProvider userDetailsProvider;

    private String serviceAuthorizationToken = "SERVICE_TOKEN";
    private String accessToken = "ACCESS_TOKEN";

    private uk.gov.hmcts.reform.ccd.document.am.model.Document uploadedDocument =
            uk.gov.hmcts.reform.ccd.document.am.model.Document.builder().build();

    private String contentType = "application/pdf";
    private String fileName = "some-file.pdf";

    private byte[] documentData = "pdf-data".getBytes();
    @Mock private Resource resource;
    private InputStream resourceInputStream = new ByteArrayInputStream(documentData);
    private String expectedDocumentUrl = "document-self-href";
    private String expectedBinaryUrl = "document-binary-href";

    @Mock private UserDetails userDetails;

    @Captor private ArgumentCaptor<List<MultipartFile>> multipartFilesCaptor;

    private DocumentManagementUploader documentManagementUploader;
    private UploadResponse uploadResponse;

    DocumentUploadRequest uploadRequest;

    @BeforeEach
    public void setUp() {

        documentManagementUploader =
            new DocumentManagementUploader(
                documentUploadClientApi,
                serviceAuthorizationTokenGenerator,
                userDetailsProvider
            );

        uploadedDocument.originalDocumentName = fileName;
        uploadedDocument.links = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Links();
        uploadedDocument.links.self = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        uploadedDocument.links.self.href = expectedDocumentUrl;
        uploadedDocument.links.binary = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        uploadedDocument.links.binary.href = expectedBinaryUrl;

        uploadResponse = new UploadResponse(List.of(uploadedDocument));

    }

    @Test
    public void should_upload_document_to_document_management_and_return_links() throws IOException {


        uploadRequest = new DocumentUploadRequest(
                "PUBLIC",
                "Asylum",
                "IA",
                multipartFilesCaptor.capture());

        when(serviceAuthorizationTokenGenerator.generate()).thenReturn(serviceAuthorizationToken);
        when(userDetails.getAccessToken()).thenReturn(accessToken);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);

        when(resource.getFilename()).thenReturn(fileName);
        when(resource.getInputStream()).thenReturn(resourceInputStream);

        when(documentUploadClientApi.uploadDocuments(
            eq(accessToken),
            eq(serviceAuthorizationToken),
            eq(uploadRequest)
        )).thenReturn(uploadResponse);

        final Document actualDocument = documentManagementUploader.upload(
            resource,
            contentType
        );

        assertEquals(fileName, actualDocument.getDocumentFilename());
        assertEquals(expectedDocumentUrl, actualDocument.getDocumentUrl());
        assertEquals(expectedBinaryUrl, actualDocument.getDocumentBinaryUrl());

        verify(documentUploadClientApi, times(1)).uploadDocuments(
            eq(accessToken),
            eq(serviceAuthorizationToken),
            eq(uploadRequest)
        );

        List<MultipartFile> actualMultipartFiles = multipartFilesCaptor.getAllValues().get(0);

        assertEquals(1, actualMultipartFiles.size());
        assertEquals(fileName, actualMultipartFiles.get(0).getName());
        assertEquals(fileName, actualMultipartFiles.get(0).getOriginalFilename());
        assertEquals(documentData.length, actualMultipartFiles.get(0).getBytes().length);
    }
}
