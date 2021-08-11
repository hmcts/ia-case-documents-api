package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.io.*;
import java.util.Date;
import java.util.List;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class DocumentManagementUploaderTest {

    @Mock private CaseDocumentClient caseDocumentClient;
    @Mock private AuthTokenGenerator serviceAuthorizationTokenGenerator;
    @Mock private UserDetailsProvider userDetailsProvider;

    private String serviceAuthorizationToken = "SERVICE_TOKEN";
    private String accessToken = "ACCESS_TOKEN";
    private String userId = "123";
    @Mock private UploadResponse uploadResponse;
    @Mock private File file;
    @Mock private DiskFileItem diskFileItem;
    @Mock private List<uk.gov.hmcts.reform.ccd.document.am.model.Document> uploadedDocuments;

    private String contentType = "application/pdf";
    private String fileName = "some-file.pdf";
    //private OutputStream outputStream = new ByteArrayOutputStream(3);

    private byte[] documentData = "pdf-data".getBytes();
    @Mock private Resource resource;
    private InputStream resourceInputStream = new ByteArrayInputStream(documentData);
    @Mock private DiskFileItem fileItem;
    private String expectedDocumentUrl = "document-self-href";
    private String expectedBinaryUrl = "document-self-href";

    private static final String SELF_LINK = "document-self-href";
    private static final String BINARY_LINK = "document-self-href";


    private static final Classification PUBLIC = Classification.PUBLIC;
    public static final String HASH_TOKEN = "aHashToken";
    public static final String MIME_TYPE = "application/octet-stream";
    public static final String ORIGINAL_DOCUMENT_NAME = "some-file.pdf";

    @Mock private UserDetails userDetails;
    @Mock private OutputStream outputStream;

    @Captor private ArgumentCaptor<List<MultipartFile>> multipartFilesCaptor;

    private DocumentManagementUploader documentManagementUploader;
    private uk.gov.hmcts.reform.ccd.document.am.model.Document mockDocument;

    @BeforeEach
    public void setUp() {

        documentManagementUploader =
            new DocumentManagementUploader(
                caseDocumentClient,
                serviceAuthorizationTokenGenerator,
                userDetailsProvider
            );

        Date ttl = new Date();

        uk.gov.hmcts.reform.ccd.document.am.model.Document.Links links = getLinks();

        mockDocument = uk.gov.hmcts.reform.ccd.document.am.model.Document.builder()
                .classification(PUBLIC)
                .hashToken(HASH_TOKEN)
                .mimeType(MIME_TYPE)
                .size(1000)
                .originalDocumentName(ORIGINAL_DOCUMENT_NAME)
                .ttl(ttl)
                .links(links)
                .build();
    }

    @Test
    public void should_upload_document_to_document_management_and_return_links() throws IOException {

        when(serviceAuthorizationTokenGenerator.generate()).thenReturn(serviceAuthorizationToken);
        when(userDetails.getAccessToken()).thenReturn(accessToken);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);

        when(resource.getFilename()).thenReturn(fileName);
        when(resource.getInputStream()).thenReturn(resourceInputStream);

        when(resource.getFilename()).thenReturn(fileName);
        when(uploadResponse.getDocuments()).thenReturn(uploadedDocuments);
        when(uploadResponse.getDocuments().get(0)).thenReturn(mockDocument);

        MockMultipartFile multipartFile = new MockMultipartFile("file1",
                "test.png",
                "application/octet-stream",
                "someBytes".getBytes());

        when(caseDocumentClient.uploadDocuments(
                eq(accessToken),
                eq(serviceAuthorizationToken),
                eq("Asylum"),
                eq("IA"),
            any(List.class)
        )).thenReturn(uploadResponse);

        final Document actualDocument = documentManagementUploader.upload(
            resource,
            contentType
        );

        assertEquals(fileName, actualDocument.getDocumentFilename());
        assertEquals(expectedDocumentUrl, actualDocument.getDocumentUrl());
        assertEquals(expectedBinaryUrl, actualDocument.getDocumentBinaryUrl());

        verify(caseDocumentClient, times(1)).uploadDocuments(
                eq(accessToken),
                eq(serviceAuthorizationToken),
                eq("Asylum"),
                eq("IA"),
            multipartFilesCaptor.capture()
        );

        List<MultipartFile> actualMultipartFiles = multipartFilesCaptor.getAllValues().get(0);

        assertEquals(1, actualMultipartFiles.size());
        assertEquals(fileName, actualMultipartFiles.get(0).getName());
        assertEquals(fileName, actualMultipartFiles.get(0).getOriginalFilename());
        assertEquals(documentData.length, actualMultipartFiles.get(0).getBytes().length);
    }

    private uk.gov.hmcts.reform.ccd.document.am.model.Document.Links getLinks() {

        uk.gov.hmcts.reform.ccd.document.am.model.Document.Links links = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Links();
        uk.gov.hmcts.reform.ccd.document.am.model.Document.Link self = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        self.href = SELF_LINK;
        uk.gov.hmcts.reform.ccd.document.am.model.Document.Link binary = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        binary.href = BINARY_LINK;
        links.self = self;
        links.binary = binary;

        return links;
    }

}
