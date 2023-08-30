package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.CdamDocumentDownloadClient;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DmDocumentDownloadClient;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadClient;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class DocumentDownloadClientTest {
    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private DmDocumentDownloadClient dmDocumentDownloadClient;
    @Mock
    private CdamDocumentDownloadClient cdamDocumentDownloadClient;

    @InjectMocks
    private DocumentDownloadClient documentDownloadClient;

    @BeforeEach
    public void setUp() {

    }

    @Test
    void should_use_cdam_when_feature_flag_true() {
        // Given
        given(featureToggler.getValue(eq("use-ccd-document-am"), anyBoolean())).willReturn(true);

        // When
        documentDownloadClient.download(null);

        // Then
        verify(cdamDocumentDownloadClient, times(1)).download(null);
    }

    @Test
    void should_use_dm_when_feature_flag_false() {
        // Given
        given(featureToggler.getValue(eq("use-ccd-document-am"), anyBoolean())).willReturn(false);

        // When
        documentDownloadClient.download(null);

        // Then
        verify(dmDocumentDownloadClient, times(1)).download(null);
    }

    @Test
    void should_use_fallback_when_cdam_fails() {
        // Given
        given(featureToggler.getValue(eq("use-ccd-document-am"), anyBoolean())).willReturn(true);
        given(featureToggler.getValue(eq("use-ccd-document-am-fallback"), anyBoolean())).willReturn(true);

        FeignException exception = mock(FeignException.class);
        given(exception.status()).willReturn(403);

        given(cdamDocumentDownloadClient.download(any())).willThrow(exception);

        // When
        documentDownloadClient.download(null);

        // Then
        verify(cdamDocumentDownloadClient, times(1)).download(null);
        verify(dmDocumentDownloadClient, times(1)).download(null);
    }

    @Test
    void should_not_use_fallback_when_cdam_fails() {
        // Given
        given(featureToggler.getValue(eq("use-ccd-document-am"), anyBoolean())).willReturn(true);
        given(featureToggler.getValue(eq("use-ccd-document-am-fallback"), anyBoolean())).willReturn(false);

        FeignException exception = mock(FeignException.class);
        given(exception.status()).willReturn(403);

        given(cdamDocumentDownloadClient.download(any())).willThrow(exception);

        // When, Then
        assertThrows(FeignException.class, () -> documentDownloadClient.download(null));

        verify(cdamDocumentDownloadClient, times(1)).download(null);
        verifyNoInteractions(dmDocumentDownloadClient);
    }

}
