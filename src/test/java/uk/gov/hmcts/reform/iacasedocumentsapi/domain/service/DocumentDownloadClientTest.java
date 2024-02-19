package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.CdamDocumentDownloadClient;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DmDocumentDownloadClient;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadClient;


import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class DocumentDownloadClientTest {
    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private DmDocumentDownloadClient dmDocumentDownloadClient;
    @Mock
    private CdamDocumentDownloadClient cdamDocumentDownloadClient;

    @InjectMocks
    private DocumentDownloadClient documentDownloadClient;

    @Test
    void should_use_cdam_when_feature_flag_true() {
        // Given
        when(featureToggler.getValue(eq("use-ccd-document-am"), eq(false))).thenReturn(true);

        // When
        documentDownloadClient.download("url");

        // Then
        verify(cdamDocumentDownloadClient, times(1)).download("url");
    }

    @Test
    void should_use_cdam_when_feature_flag_false() {
        // Given
        when(featureToggler.getValue(eq("use-ccd-document-am"), eq(false))).thenReturn(false);

        // When
        documentDownloadClient.download("url");

        // Then
        verify(dmDocumentDownloadClient, times(1)).download("url");
    }
}
