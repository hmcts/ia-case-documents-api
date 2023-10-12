package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.CdamDocumentDownloadClient;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.AccessTokenProvider;


@ExtendWith(MockitoExtension.class)
public class CdamDocumentDownloadClientTest {

    private final String someAccessToken = "some-access-token";
    private final String someServiceAuthToken = "some-service-auth-token";

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @Mock private AccessTokenProvider accessTokenProvider;
    @Mock private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock private UserDetailsProvider userDetailsProvider;
    @Mock private UserDetails userDetails;
    @Mock private ResponseEntity<Resource> responseEntity;
    @Mock private Resource downloadedResource;

    private CdamDocumentDownloadClient cdamDocumentDownloadClient;
    private String someWellFormattedDocumentBinaryDownloadUrl = "https://host:8080/a/b/c";
    private String someUserRolesString = "some-role,some-other-role";
    private String someUserId = "some-user-id";

    @BeforeEach
    public void setUp() {
        cdamDocumentDownloadClient = new CdamDocumentDownloadClient(
            caseDocumentClient,
            serviceAuthTokenGenerator,
            accessTokenProvider
        );

    }

    @Test
    public void downloads_resource() {
        when(caseDocumentClient.getDocumentBinary(
            someAccessToken,
            someServiceAuthToken,
            "https://host:8080/a/b/c")).thenReturn(responseEntity);

        when(responseEntity.getBody())
            .thenReturn(downloadedResource);
        when(accessTokenProvider.getAccessToken())
            .thenReturn(someAccessToken);
        when(serviceAuthTokenGenerator.generate())
            .thenReturn(someServiceAuthToken);
        Resource resource = cdamDocumentDownloadClient.download(someWellFormattedDocumentBinaryDownloadUrl);
        verify(caseDocumentClient, times(1))
            .getDocumentBinary(
                eq(someAccessToken),
                eq(someServiceAuthToken),
                eq("https://host:8080/a/b/c")
            );
        assertEquals(resource, downloadedResource);
    }

    @Test
    public void throws_if_document_binary_url_bad() {

        assertThatThrownBy(() -> cdamDocumentDownloadClient.download("bad-url"))
            .isInstanceOfAny(IllegalStateException.class, NullPointerException.class);
    }

    @Test
    public void throws_if_document_api_returns_empty_body() {
        when(caseDocumentClient.getDocumentBinary(
            someAccessToken,
            someServiceAuthToken,
            "https://host:8080/a/b/c")).thenReturn(responseEntity);

        when(responseEntity.getBody())
            .thenReturn(downloadedResource);
        when(accessTokenProvider.getAccessToken())
            .thenReturn(someAccessToken);
        when(serviceAuthTokenGenerator.generate())
            .thenReturn(someServiceAuthToken);
        when(responseEntity.getBody())
            .thenReturn(null);

        assertThatThrownBy(() -> cdamDocumentDownloadClient.download(someWellFormattedDocumentBinaryDownloadUrl))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Document could not be downloaded");
    }
}

