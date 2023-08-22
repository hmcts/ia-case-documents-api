package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CdamDocumentManagementUploaderTest {

    @Mock private CaseDocumentClient caseDocumentClient;
    @Mock private AuthTokenGenerator serviceAuthorizationTokenGenerator;
    @Mock private UserDetailsProvider userDetailsProvider;

    @InjectMocks
    private CdamDocumentManagementUploader documentManagementUploader;

    @BeforeEach
    public void setUp() {
        UserDetails userDetails = mock(UserDetails.class);

        given(serviceAuthorizationTokenGenerator.generate()).willReturn("some token");
        given(userDetailsProvider.getUserDetails()).willReturn(userDetails);
        given(userDetails.getAccessToken()).willReturn("some access token");
    }

    @Test
    void should_invoke_client_when_upload_requested() throws IOException {
        // Given
        Resource resource = mock(Resource.class);
        given(resource.getFilename()).willReturn("somefile.txt");
        given(resource.getInputStream()).willReturn(new ByteArrayInputStream(new byte[] {}));

        uk.gov.hmcts.reform.ccd.document.am.model.Document expectedDocument =
            uk.gov.hmcts.reform.ccd.document.am.model.Document.builder()
                .build();
        expectedDocument.links = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Links();
        expectedDocument.links.self = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        expectedDocument.links.binary = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        expectedDocument.links.self.href = "self href";
        expectedDocument.links.binary.href = "binary href";
        expectedDocument.originalDocumentName = "somefile.txt";

        List<uk.gov.hmcts.reform.ccd.document.am.model.Document> documents = new ArrayList<>();
        documents.add(expectedDocument);

        UploadResponse uploadResponse = new UploadResponse(documents);

        given(caseDocumentClient.uploadDocuments(eq("some access token"),
            eq("some token"),
            eq("Asylum"),
            eq("IA"),
            any())).willReturn(uploadResponse);

        // When
        Document result = documentManagementUploader.upload(resource, "application/text");

        // Then
        verify(caseDocumentClient, times(1)).uploadDocuments(eq("some access token"),
            eq("some token"),
            eq("Asylum"),
            eq("IA"),
            any());
        assertThat(result.getDocumentBinaryUrl()).isEqualTo(expectedDocument.links.binary.href);

    }

    @Test
    void should_throw_exception_when_no_document() throws IOException {
        // Given
        Resource resource = mock(Resource.class);
        given(resource.getFilename()).willReturn("somefile.txt");
        given(resource.getInputStream()).willReturn(new ByteArrayInputStream(new byte[] {}));

        List<uk.gov.hmcts.reform.ccd.document.am.model.Document> documents = new ArrayList<>();

        UploadResponse uploadResponse = new UploadResponse(documents);

        given(caseDocumentClient.uploadDocuments(eq("some access token"),
            eq("some token"),
            eq("Asylum"),
            eq("IA"),
            any())).willReturn(uploadResponse);

        // When
        assertThrows(DocumentServiceResponseException.class, () -> {
            documentManagementUploader.upload(resource,  "application/text");
        });

        // Then
        verify(caseDocumentClient, times(1)).uploadDocuments(eq("some access token"),
            eq("some token"),
            eq("Asylum"),
            eq("IA"),
            any());

    }

}
