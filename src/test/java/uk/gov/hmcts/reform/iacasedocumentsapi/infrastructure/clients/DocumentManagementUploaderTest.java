package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

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
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;


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

    @Mock private List<uk.gov.hmcts.reform.ccd.document.am.model.Document> uploadedDocuments;
    private uk.gov.hmcts.reform.ccd.document.am.model.Document uploadedDocument = Document.builder().build();

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

    @BeforeEach
    public void setUp() {

        documentManagementUploader =
                new DocumentManagementUploader(
                        caseDocumentClient,
                        serviceAuthorizationTokenGenerator,
                        userDetailsProvider
                );

        uploadedDocument.originalDocumentName = fileName;
        uploadedDocument.links = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Links();
        uploadedDocument.links.self = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        uploadedDocument.links.self.href = expectedDocumentUrl;
        uploadedDocument.links.binary = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        uploadedDocument.links.binary.href = expectedBinaryUrl;
    }

    @Test
    public void should_upload_document_to_document_management_and_return_links() throws IOException {

        verify(caseDocumentClient, times(0)).uploadDocuments(
                eq(accessToken),
                eq(serviceAuthorizationToken),
                eq("Asylum"),
                eq("IA"),
                multipartFilesCaptor.capture()
        );

    }
}
