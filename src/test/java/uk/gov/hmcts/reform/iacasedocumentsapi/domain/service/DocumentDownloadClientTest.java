package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.CcdCaseDocumentAmClient;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.AccessTokenProvider;

@ExtendWith(MockitoExtension.class)
public class DocumentDownloadClientTest {

    private final String someAccessToken = "some-access-token";
    private final String someServiceAuthToken = "some-service-auth-token";

    @Mock
    private CcdCaseDocumentAmClient caseDocumentClient;
    @Mock private AccessTokenProvider accessTokenProvider;
    @Mock private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock private ResponseEntity<Resource> responseEntity;
    @Mock private Resource downloadedResource;

    private DocumentDownloadClient documentDownloadClient;
    private String someWellFormattedDocumentBinaryDownloadUrl = "http://host:8080/a24a6ea4-ce75-4665-a070-57453082c256";

    @BeforeEach
    public void setUp() {
        documentDownloadClient = new DocumentDownloadClient(
            caseDocumentClient,
            serviceAuthTokenGenerator,
            accessTokenProvider);
    }

    @Test
    public void downloads_resource() {

        when(caseDocumentClient.getDocumentBinary(
                someAccessToken,
                someServiceAuthToken,
                UUID.fromString("a24a6ea4-ce75-4665-a070-57453082c256"))).thenReturn(responseEntity);

        when(responseEntity.getBody())
                .thenReturn(downloadedResource);

        when(accessTokenProvider.getAccessToken())
                .thenReturn(someAccessToken);

        when(serviceAuthTokenGenerator.generate())
                .thenReturn(someServiceAuthToken);

        Resource resource = documentDownloadClient.download(someWellFormattedDocumentBinaryDownloadUrl);

        verify(caseDocumentClient, times(1))
            .getDocumentBinary(
                eq(someAccessToken),
                eq(someServiceAuthToken),
                eq(UUID.fromString("a24a6ea4-ce75-4665-a070-57453082c256"))
            );

        assertEquals(resource, downloadedResource);
    }

    @Test
    public void throws_if_document_binary_url_bad() {

        assertThatThrownBy(() -> documentDownloadClient.download("bad-url"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid url for CaseDocumentClient");

        verifyNoInteractions(caseDocumentClient);
        verifyNoInteractions(serviceAuthTokenGenerator);
        verifyNoInteractions(accessTokenProvider);
    }

    @Test
    public void throws_if_document_api_returns_empty_body() {

        when(caseDocumentClient.getDocumentBinary(
                someAccessToken,
                someServiceAuthToken,
                UUID.fromString("a24a6ea4-ce75-4665-a070-57453082c256"))).thenReturn(responseEntity);

        when(responseEntity.getBody())
                .thenReturn(downloadedResource);

        when(accessTokenProvider.getAccessToken())
                .thenReturn(someAccessToken);

        when(serviceAuthTokenGenerator.generate())
                .thenReturn(someServiceAuthToken);

        when(responseEntity.getBody())
            .thenReturn(null);

        assertThatThrownBy(() -> documentDownloadClient.download(someWellFormattedDocumentBinaryDownloadUrl))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Document could not be downloaded");
    }

}
