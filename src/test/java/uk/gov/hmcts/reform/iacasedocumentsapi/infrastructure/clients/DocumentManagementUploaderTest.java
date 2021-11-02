package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;

@ExtendWith(MockitoExtension.class)
class DocumentManagementUploaderTest {

    @Mock private CaseDocumentClientApi caseDocumentClientApi;
    @Mock private AuthTokenGenerator serviceAuthorizationTokenGenerator;
    @Mock private UserDetailsProvider userDetailsProvider;

    private final String serviceAuthorizationToken = "SERVICE_TOKEN";
    private final String accessToken = "ACCESS_TOKEN";
    private final String userId = "123";
    @Mock private UploadResponse uploadResponse;
    private uk.gov.hmcts.reform.ccd.document.am.model.Document uploadedDocument;


    private final String contentType = "application/pdf";
    private final String fileName = "some-file.pdf";

    private final byte[] documentData = "pdf-data".getBytes();
    @Mock private Resource resource;
    private final InputStream resourceInputStream = new ByteArrayInputStream(documentData);
    private final String expectedDocumentUrl = "document-self-href";
    private final String expectedBinaryUrl = "document-binary-href";
    private final String classification = Classification.PUBLIC.name();
    private final String caseTypeId = "Asylum";
    private final String jurisdictionId = "some-juirsdictionId";

    @Mock private UserDetails userDetails;

    @Captor private ArgumentCaptor<DocumentUploadRequest> documentUploadRequestCaptor;

    private DocumentManagementUploader documentManagementUploader;


    @BeforeEach
    public void setUp() {

        documentManagementUploader =
            new DocumentManagementUploader(
                caseDocumentClientApi,
                serviceAuthorizationTokenGenerator,
                userDetailsProvider
            );

        uk.gov.hmcts.reform.ccd.document.am.model.Document.Link selfLink = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        selfLink.href = expectedDocumentUrl;

        uk.gov.hmcts.reform.ccd.document.am.model.Document.Link binaryLink = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        binaryLink.href = expectedBinaryUrl;

        uk.gov.hmcts.reform.ccd.document.am.model.Document.Links links = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Links();
        links.binary = binaryLink;
        links.self = selfLink;

        uploadedDocument =   uk.gov.hmcts.reform.ccd.document.am.model.Document.builder()
            .originalDocumentName(fileName)
            .classification(Classification.PUBLIC)
            .hashToken(UUID.randomUUID().toString())
            .links(links)
            .build();
    }

    @Test
    void should_upload_document_to_document_management_and_return_links() throws IOException {

        when(serviceAuthorizationTokenGenerator.generate()).thenReturn(serviceAuthorizationToken);
        when(userDetails.getAccessToken()).thenReturn(accessToken);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);

        when(resource.getFilename()).thenReturn(fileName);
        when(resource.getInputStream()).thenReturn(resourceInputStream);

        when(uploadResponse.getDocuments()).thenReturn(Collections.singletonList(uploadedDocument));

        when(caseDocumentClientApi.uploadDocuments(
            eq(accessToken),
            eq(serviceAuthorizationToken),
            any()
        )).thenReturn(uploadResponse);

        final Document actualDocument = documentManagementUploader.upload(
            resource,
            classification,
            caseTypeId,
            jurisdictionId,
            contentType
        );

        assertEquals(fileName, actualDocument.getDocumentFilename());
        assertEquals(expectedDocumentUrl, actualDocument.getDocumentUrl());
        assertEquals(expectedBinaryUrl, actualDocument.getDocumentBinaryUrl());

        verify(caseDocumentClientApi, times(1)).uploadDocuments(
            eq(accessToken),
            eq(serviceAuthorizationToken),
            documentUploadRequestCaptor.capture()
        );

        DocumentUploadRequest documentUploadRequest = documentUploadRequestCaptor.getAllValues().get(0);

        assertNotNull(documentUploadRequest);
        assertEquals(1, documentUploadRequest.getFiles().size());
        assertEquals(documentData.length, documentUploadRequest.getFiles().get(0).getBytes().length);
        assertEquals(classification,documentUploadRequest.getClassification());
        assertEquals(caseTypeId,documentUploadRequest.getCaseTypeId());
        assertEquals(jurisdictionId,documentUploadRequest.getJurisdictionId());
    }
}
