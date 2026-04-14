package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DocumentManagementUploaderTest {

    @Mock
    private CdamDocumentManagementUploader cdamDocumentManagementUploader;

    @InjectMocks
    private DocumentManagementUploader documentManagementUploader;

    @BeforeEach
    public void setUp() {

    }

    @Test
    void should_use_cdam() throws IOException {
        // Given
        Resource resource = mock(Resource.class);
        given(resource.getFilename()).willReturn("file.ext");

        // When
        documentManagementUploader.upload(resource, null);

        // Then
        verify(cdamDocumentManagementUploader, times(1)).upload(resource, null);
    }

}
