package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.CdamDocumentDownloadClient;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DmDocumentDownloadClient;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadClient;


import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

    @BeforeEach
    public void setUp() {

    }

    @Test
    void should_use_cdam_when_feature_flag_true() throws IOException {

        // When
        documentDownloadClient.download(null);

        // Then
        verify(cdamDocumentDownloadClient, times(1)).download(null);
    }
}
