package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class DocumentManagementUploaderTest {

    @Mock private DocumentUploadClientApi documentUploadClientApi;
    @Mock private AuthTokenGenerator serviceAuthorizationTokenGenerator;
    @Mock private UserDetailsProvider userDetailsProvider;

    private String serviceAuthorizationToken = "SERVICE_TOKEN";
    private String accessToken = "ACCESS_TOKEN";
    private String userId = "123";
    @Mock private UploadResponse uploadResponse;
    @Mock private UploadResponse.Embedded uploadResponseEmbedded;
    @Mock private List<uk.gov.hmcts.reform.document.domain.Document> uploadedDocuments;
    private uk.gov.hmcts.reform.document.domain.Document uploadedDocument
        = new uk.gov.hmcts.reform.document.domain.Document();

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

    @Before
    public void setUp() {

        documentManagementUploader =
            new DocumentManagementUploader(
                documentUploadClientApi,
                serviceAuthorizationTokenGenerator,
                userDetailsProvider
            );

        uploadedDocument.originalDocumentName = fileName;
        uploadedDocument.links = new uk.gov.hmcts.reform.document.domain.Document.Links();
        uploadedDocument.links.self = new uk.gov.hmcts.reform.document.domain.Document.Link();
        uploadedDocument.links.self.href = expectedDocumentUrl;
        uploadedDocument.links.binary = new uk.gov.hmcts.reform.document.domain.Document.Link();
        uploadedDocument.links.binary.href = expectedBinaryUrl;
    }

    @Test
    public void should_upload_document_to_document_management_and_return_links() throws IOException {

        when(serviceAuthorizationTokenGenerator.generate()).thenReturn(serviceAuthorizationToken);
        when(userDetails.getAccessToken()).thenReturn(accessToken);
        when(userDetails.getId()).thenReturn(userId);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);

        when(resource.getFilename()).thenReturn(fileName);
        when(resource.getInputStream()).thenReturn(resourceInputStream);

        when(uploadResponse.getEmbedded()).thenReturn(uploadResponseEmbedded);
        when(uploadResponseEmbedded.getDocuments()).thenReturn(uploadedDocuments);
        when(uploadedDocuments.get(0)).thenReturn(uploadedDocument);

        when(documentUploadClientApi.upload(
            eq(accessToken),
            eq(serviceAuthorizationToken),
            eq(userId),
            any(List.class)
        )).thenReturn(uploadResponse);

        final Document actualDocument = documentManagementUploader.upload(
            resource,
            contentType
        );

        assertEquals(fileName, actualDocument.getDocumentFilename());
        assertEquals(expectedDocumentUrl, actualDocument.getDocumentUrl());
        assertEquals(expectedBinaryUrl, actualDocument.getDocumentBinaryUrl());

        verify(documentUploadClientApi, times(1)).upload(
            eq(accessToken),
            eq(serviceAuthorizationToken),
            eq(userId),
            multipartFilesCaptor.capture()
        );

        List<MultipartFile> actualMultipartFiles = multipartFilesCaptor.getAllValues().get(0);

        assertEquals(1, actualMultipartFiles.size());
        assertEquals(fileName, actualMultipartFiles.get(0).getName());
        assertEquals(fileName, actualMultipartFiles.get(0).getOriginalFilename());
        assertEquals(documentData.length, actualMultipartFiles.get(0).getBytes().length);
    }
}
