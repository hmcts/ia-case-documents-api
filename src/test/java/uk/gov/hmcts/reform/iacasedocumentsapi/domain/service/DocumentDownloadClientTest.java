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
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.AccessTokenProvider;

@ExtendWith(MockitoExtension.class)
class DocumentDownloadClientTest {

    private final String someAccessToken = "some-access-token";
    private final String someServiceAuthToken = "some-service-auth-token";

    @Mock
    private CaseDocumentClientApi caseDocumentClientApi;
    @Mock private AccessTokenProvider accessTokenProvider;
    @Mock private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock private ResponseEntity<Resource> responseEntity;
    @Mock private Resource downloadedResource;

    private DocumentDownloadClient documentDownloadClient;
    private final UUID someUuid = UUID.randomUUID();
    private final String someWellFormattedDocumentBinaryDownloadUrl = String.format("http://host:8080/document/%s/binary", someUuid);

    @BeforeEach
    public void setUp() {
        documentDownloadClient = new DocumentDownloadClient(
            caseDocumentClientApi,
            serviceAuthTokenGenerator,
            accessTokenProvider);
    }

    @Test
    void downloads_resource() {

        when(caseDocumentClientApi.getDocumentBinary(
                someAccessToken,
                someServiceAuthToken,
            someUuid)).thenReturn(responseEntity);

        when(responseEntity.getBody())
                .thenReturn(downloadedResource);

        when(accessTokenProvider.getAccessToken())
                .thenReturn(someAccessToken);

        when(serviceAuthTokenGenerator.generate())
                .thenReturn(someServiceAuthToken);

        Resource resource = documentDownloadClient.download(someWellFormattedDocumentBinaryDownloadUrl);

        verify(caseDocumentClientApi, times(1))
            .getDocumentBinary(
                eq(someAccessToken),
                eq(someServiceAuthToken),
                eq(someUuid)
            );

        assertEquals(resource, downloadedResource);
    }

    @Test
    void throws_if_document_binary_url_bad() {

        assertThatThrownBy(() -> documentDownloadClient.download("bad-url"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid url for DocumentDownloadClientApi");

        verifyNoInteractions(caseDocumentClientApi);
        verifyNoInteractions(serviceAuthTokenGenerator);
        verifyNoInteractions(accessTokenProvider);
    }

    @Test
    void throws_if_document_api_returns_empty_body() {

        when(caseDocumentClientApi.getDocumentBinary(
                someAccessToken,
                someServiceAuthToken,
            someUuid)).thenReturn(responseEntity);

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
