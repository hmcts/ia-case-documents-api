package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.AccessTokenProvider;

@RunWith(MockitoJUnitRunner.class)
public class DocumentDownloadClientTest {

    private final String someAccessToken = "some-access-token";
    private final String someServiceAuthToken = "some-service-auth-token";

    @Mock
    private DocumentDownloadClientApi documentDownloadClientApi;
    @Mock private AccessTokenProvider accessTokenProvider;
    @Mock private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock private UserDetailsProvider userDetailsProvider;
    @Mock private UserDetails userDetails;
    @Mock private ResponseEntity<Resource> responseEntity;
    @Mock private Resource downloadedResource;

    private DocumentDownloadClient documentDownloadClient;
    private String someWellFormattedDocumentBinaryDownloadUrl = "http://host:8080/a/b/c";
    private String someUserRolesString = "some-role,some-other-role";
    private String someUserId = "some-user-id";

    @Before
    public void setUp() {
        documentDownloadClient = new DocumentDownloadClient(
            documentDownloadClientApi,
            serviceAuthTokenGenerator,
            accessTokenProvider,
            userDetailsProvider);

        when(documentDownloadClientApi.downloadBinary(
            someAccessToken,
            someServiceAuthToken,
            someUserRolesString,
            someUserId,
            "a/b/c")).thenReturn(responseEntity);

        when(responseEntity.getBody())
            .thenReturn(downloadedResource);

        when(accessTokenProvider.getAccessToken())
            .thenReturn(someAccessToken);

        when(serviceAuthTokenGenerator.generate())
            .thenReturn(someServiceAuthToken);

        when(userDetailsProvider.getUserDetails())
            .thenReturn(userDetails);

        when(userDetails.getRoles())
            .thenReturn(asList(someUserRolesString));

        when(userDetails.getId())
            .thenReturn(someUserId);
    }

    @Test
    public void downloads_resource() {

        Resource resource = documentDownloadClient.download(someWellFormattedDocumentBinaryDownloadUrl);

        verify(documentDownloadClientApi, times(1))
            .downloadBinary(
                eq(someAccessToken),
                eq(someServiceAuthToken),
                eq(someUserRolesString),
                eq(someUserId),
                eq("a/b/c")
            );

        assertThat(resource).isEqualTo(downloadedResource);
    }

    @Test
    public void throws_if_document_binary_url_bad() {

        assertThatThrownBy(() -> documentDownloadClient.download("bad-url"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid url for DocumentDownloadClientApi");

        verifyNoInteractions(documentDownloadClientApi);
        verifyNoInteractions(serviceAuthTokenGenerator);
        verifyNoInteractions(accessTokenProvider);
    }

    @Test
    public void throws_if_document_api_returns_empty_body() {

        when(responseEntity.getBody())
            .thenReturn(null);

        assertThatThrownBy(() -> documentDownloadClient.download(someWellFormattedDocumentBinaryDownloadUrl))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Document could not be downloaded");
    }
}