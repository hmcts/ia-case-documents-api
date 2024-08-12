package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentManagementUploader;

import java.io.File;
import java.io.IOException;

import static java.io.File.createTempFile;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaveNotificationsToDataPdfServiceTest {

    @Mock
    private Document pdf;
    @Mock
    private File mockPdfFile;
    @Mock
    private DocumentManagementUploader documentUploader;
    @Mock
    private DocumentToPdfConverter documentToPdfConverter;

    private SaveNotificationsToDataPdfService saveNotificationsToDataPdfService;
    private final String notificationBody = "<div>some-content</div>";
    private final String notificationReference = "test-reference";

    @BeforeEach
    public void setUp() {
        saveNotificationsToDataPdfService =
            new SaveNotificationsToDataPdfService(documentUploader, documentToPdfConverter);
    }

    @Test
    void should_create_pdf_and_upload_successfully() throws IOException {
        mockPdfFile = createTempFile("test-file", ".pdf");
        when(documentToPdfConverter.convertHtmlDocResourceToPdf(any(Resource.class))).thenReturn(mockPdfFile);
        when(documentUploader.upload(any(ByteArrayResource.class), anyString())).thenReturn(pdf);

        Document result = saveNotificationsToDataPdfService.createPdf(notificationBody, notificationReference);

        assertNotNull(result);
        verify(documentToPdfConverter).convertHtmlDocResourceToPdf(any(Resource.class));
        verify(documentUploader).upload(any(ByteArrayResource.class), eq("application/pdf"));
    }

    @Test
    void should_throw_if_byte_array_throws() throws IOException {
        try (MockedStatic<FileUtils> utilities = mockStatic(FileUtils.class)) {
            mockPdfFile = createTempFile("test-file", ".pdf");
            when(documentToPdfConverter.convertHtmlDocResourceToPdf(any(Resource.class))).thenReturn(mockPdfFile);
            utilities.when(() -> FileUtils.readFileToByteArray(mockPdfFile))
                .thenThrow(new IOException("exception"));
            assertThatThrownBy(() -> saveNotificationsToDataPdfService.createPdf(notificationBody, notificationReference))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Error reading converted pdf");
            verify(documentToPdfConverter, times(1)).convertHtmlDocResourceToPdf(any(Resource.class));
            verify(documentUploader, times(0)).upload(any(ByteArrayResource.class), eq("application/pdf"));
        }
    }
}
