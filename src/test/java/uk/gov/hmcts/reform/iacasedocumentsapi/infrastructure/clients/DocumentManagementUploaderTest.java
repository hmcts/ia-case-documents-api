package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DocumentManagementUploaderTest {

    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private DmDocumentManagementUploader dmDocumentManagementUploader;
    @Mock
    private CdamDocumentManagementUploader cdamDocumentManagementUploader;

    @Mock
    private Resource resource;

    @InjectMocks
    private DocumentManagementUploader documentManagementUploader;

    @Test
    void should_use_cdam_when_feature_flag_true() {
        // Given
        when(featureToggler.getValue(eq("use-ccd-document-am"), eq(false))).thenReturn(true);

        // When
        documentManagementUploader.upload(resource, "Asylum", "IA", "application/pdf");

        // Then
        verify(cdamDocumentManagementUploader, times(1)).upload(eq(resource), eq("Asylum"), eq("IA"), eq("application/pdf"));
    }

    @Test
    void should_use_cdam_when_feature_flag_false() {
        // Given
        when(featureToggler.getValue(eq("use-ccd-document-am"), eq(false))).thenReturn(false);

        // When
        documentManagementUploader.upload(resource, "Asylum", "IA", "application/pdf");

        // Then
        verify(dmDocumentManagementUploader, times(1)).upload(eq(resource), eq("application/pdf"));
    }
}
