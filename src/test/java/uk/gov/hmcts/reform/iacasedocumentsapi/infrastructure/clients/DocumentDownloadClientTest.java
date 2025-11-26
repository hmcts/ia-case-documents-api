package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import static java.util.Arrays.asList;
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
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.AccessTokenProvider;

@ExtendWith(MockitoExtension.class)
public class DocumentDownloadClientTest {
    @Mock
    private Resource downloadedResource;
    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private DmDocumentDownloadClient dmDocumentDownloadClient;
    @Mock
    private CdamDocumentDownloadClient cdamDocumentDownloadClient;
    private DocumentDownloadClient documentDownloadClient;
    private final String someWellFormattedDocumentBinaryDownloadUrl = "http://host:8080/a/b/c";

    @BeforeEach
    public void setUp() {
        documentDownloadClient = new DocumentDownloadClient(
            featureToggler,
            dmDocumentDownloadClient,
            cdamDocumentDownloadClient
        );
    }

    @Test
    public void usesCdamIfFlagOn() {
        when(featureToggler.getValue("use-ccd-document-am", false)).thenReturn(true);
        when(cdamDocumentDownloadClient.download(anyString()))
            .thenReturn(downloadedResource);

        Resource resource = documentDownloadClient.download(someWellFormattedDocumentBinaryDownloadUrl);

        verify(cdamDocumentDownloadClient, times(1))
            .download(someWellFormattedDocumentBinaryDownloadUrl);

        verify(dmDocumentDownloadClient, never())
            .download(someWellFormattedDocumentBinaryDownloadUrl);

        assertEquals(resource, downloadedResource);
    }

    @Test
    public void usesDmIfFlagOff() {
        when(featureToggler.getValue("use-ccd-document-am", false)).thenReturn(false);
        when(dmDocumentDownloadClient.download(anyString()))
            .thenReturn(downloadedResource);

        Resource resource = documentDownloadClient.download(someWellFormattedDocumentBinaryDownloadUrl);

        verify(dmDocumentDownloadClient, times(1))
            .download(someWellFormattedDocumentBinaryDownloadUrl);

        verify(cdamDocumentDownloadClient, never())
            .download(someWellFormattedDocumentBinaryDownloadUrl);

        assertEquals(resource, downloadedResource);
    }

    @Test
    public void usesDmIfNoFlag() {
        when(dmDocumentDownloadClient.download(anyString()))
            .thenReturn(downloadedResource);

        Resource resource = documentDownloadClient.download(someWellFormattedDocumentBinaryDownloadUrl);

        verify(dmDocumentDownloadClient, times(1))
            .download(someWellFormattedDocumentBinaryDownloadUrl);

        verify(cdamDocumentDownloadClient, never())
            .download(someWellFormattedDocumentBinaryDownloadUrl);

        assertEquals(resource, downloadedResource);
    }


}
