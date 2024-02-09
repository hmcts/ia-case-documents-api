package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DocumentManagementUploaderTest {

    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private DmDocumentManagementUploader dmDocumentManagementUploader;
    @Mock
    private CdamDocumentManagementUploader cdamDocumentManagementUploader;

    @InjectMocks
    private DocumentManagementUploader documentManagementUploader;

    @BeforeEach
    public void setUp() {

    }

    @Test
    void should_use_cdam_when_feature_flag_true() throws IOException {
        // Given
        // When
        documentManagementUploader.upload(null, null, null, null, null);

        // Then
        verify(cdamDocumentManagementUploader, times(1)).upload(null, null);
    }
}
