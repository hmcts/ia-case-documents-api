package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.StoredNotification;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;

import java.io.File;
import java.io.IOException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

import static java.io.File.createTempFile;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaveNotificationsToDataPdfServiceTest {

    @Mock
    private Document pdf;
    @Mock
    private File mockPdfFile;
    @Mock
    private DocumentUploader documentUploader;
    @Mock
    private DocumentToPdfConverter documentToPdfConverter;
    @Mock
    private StoredNotification mockedStoredNotification;

    private final String reference = "test-reference";
    private final String subject = "someSubject";
    private final String notificationId = "someNotificationId";
    private final String body = "<div>some-content</div>";
    private final String notificationType = "someNotificationType";
    private final String status = "someStatus";
    private final String email = "some-email@test.com";

    private SaveNotificationsToDataPdfService saveNotificationsToDataPdfService;

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

        Document result = saveNotificationsToDataPdfService.createPdf(body, reference +  ".PDF");

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
            assertThatThrownBy(() -> saveNotificationsToDataPdfService.createPdf(body, reference))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Error reading converted pdf");
            verify(documentToPdfConverter, times(1)).convertHtmlDocResourceToPdf(any(Resource.class));
            verify(documentUploader, times(0)).upload(any(ByteArrayResource.class), eq("application/pdf"));
        }
    }

    @Test
    void should_write_empty_list_if_no_stored_notifications() {
        List<IdValue<StoredNotification>> response =
            saveNotificationsToDataPdfService.generatePdfsForNotifications(emptyList());
        assertEquals(0, response.size());
    }

    @Test
    void should_not_change_notification_if_document_not_null() {
        StoredNotification storedNotification =
            StoredNotification.builder()
                .notificationId(notificationId)
                .notificationDateSent("2024-01-01")
                .notificationSentTo(email)
                .notificationBody("<div>" + body + "</div>")
                .notificationMethod(notificationType)
                .notificationStatus(status)
                .notificationReference(reference)
                .notificationSubject(subject)
                .build();
        storedNotification.setNotificationDocument(pdf);
        List<IdValue<StoredNotification>> storedNotifications =
            List.of(new IdValue<>(reference, storedNotification));

        List<IdValue<StoredNotification>> response =
            saveNotificationsToDataPdfService.generatePdfsForNotifications(storedNotifications);
        assertEquals(1, response.size());
        assertEquals(storedNotifications, response);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Cancelled", "Failed", "Technical-failure",
        "Temporary-failure", "Permanent-failure", "Validation-failed", "Virus-scan-failed"})
    void should_not_change_notification_if_status_invalid(String invalidStatus) {
        StoredNotification storedNotification =
            StoredNotification.builder()
                .notificationId(notificationId)
                .notificationDateSent("2024-01-01")
                .notificationSentTo(email)
                .notificationBody("<div>" + body + "</div>")
                .notificationMethod(notificationType)
                .notificationStatus(invalidStatus)
                .notificationReference(reference)
                .notificationSubject(subject)
                .build();
        List<IdValue<StoredNotification>> storedNotifications =
            List.of(new IdValue<>(reference, storedNotification));

        List<IdValue<StoredNotification>> response =
            saveNotificationsToDataPdfService.generatePdfsForNotifications(storedNotifications);
        assertEquals(1, response.size());
        assertEquals(storedNotifications, response);
    }

    @Test
    void should_set_notification_document_if_valid() throws IOException {
        List<IdValue<StoredNotification>> storedNotifications =
            List.of(new IdValue<>(reference, mockedStoredNotification));
        when(mockedStoredNotification.getNotificationStatus()).thenReturn(status);
        when(mockedStoredNotification.getNotificationBody()).thenReturn(body);
        when(mockedStoredNotification.getNotificationReference()).thenReturn(reference);
        when(documentToPdfConverter.convertHtmlDocResourceToPdf(any())).thenReturn(createPdfFile());
        when(documentUploader.upload(any(), any())).thenReturn(pdf);

        List<IdValue<StoredNotification>> response =
            saveNotificationsToDataPdfService.generatePdfsForNotifications(storedNotifications);

        verify(mockedStoredNotification, times(1)).setNotificationDocument(pdf);
        assertEquals(1, response.size());
        for (IdValue<StoredNotification> idValue : response) {
            assertEquals(reference, idValue.getId());
            assertEquals(mockedStoredNotification, idValue.getValue());
        }
    }

    @Test
    void should_generate_and_add_document_to_notification() throws IOException {
        StoredNotification storedNotification =
            StoredNotification.builder()
                .notificationId(notificationId)
                .notificationDateSent("2024-01-01")
                .notificationSentTo(email)
                .notificationBody("<div>" + body + "</div>")
                .notificationMethod(notificationType)
                .notificationStatus(status)
                .notificationReference(reference)
                .notificationSubject(subject)
                .build();
        List<IdValue<StoredNotification>> storedNotifications =
            List.of(new IdValue<>(reference, storedNotification));

        when(documentToPdfConverter.convertHtmlDocResourceToPdf(any())).thenReturn(createPdfFile());
        when(documentUploader.upload(any(), any())).thenReturn(pdf);

        List<IdValue<StoredNotification>> response =
            saveNotificationsToDataPdfService.generatePdfsForNotifications(storedNotifications);

        assertEquals(1, response.size());
        for (IdValue<StoredNotification> idValue : response) {
            assertEquals(reference, idValue.getId());
            assertEquals(storedNotification, idValue.getValue());
        }

        assertNotNull(response.get(0).getValue().getNotificationDocument());
        assertEquals(pdf, response.get(0).getValue().getNotificationDocument());
    }

    private File createPdfFile() throws IOException {
        return File.createTempFile("test-file", ".pdf");
    }
}
