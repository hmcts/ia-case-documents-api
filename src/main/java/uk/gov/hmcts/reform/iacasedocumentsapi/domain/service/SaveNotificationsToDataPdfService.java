package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import java.util.Base64;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.StoredNotification;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

@Service
public class SaveNotificationsToDataPdfService {

    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final DocumentToPdfConverter documentToPdfConverter;
    private final DocumentUploader documentUploader;

    public SaveNotificationsToDataPdfService(
        DocumentUploader documentUploader,
        DocumentToPdfConverter documentToPdfConverter
    ) {
        this.documentUploader = documentUploader;
        this.documentToPdfConverter = documentToPdfConverter;
    }

    public Document createPdf(String notificationBody, String notificationReference) {

        byte[] byteArray = notificationBody.getBytes();
        Resource resource = new ByteArrayResource(byteArray);

        File notificationPdf =
            documentToPdfConverter.convertHtmlDocResourceToPdf(resource);

        ByteArrayResource byteArrayResource = getByteArrayResourceFromFile(
            notificationPdf,
            notificationReference + ".PDF"
        );

        return documentUploader.upload(byteArrayResource, PDF_CONTENT_TYPE);
    }

    private ByteArrayResource getByteArrayResourceFromFile(File notificationPdf, String filename) {

        byte[] byteArray;

        try {
            byteArray = FileUtils.readFileToByteArray(notificationPdf);

        } catch (IOException e) {
            throw new IllegalStateException("Error reading converted pdf");
        }

        return getByteArrayResource(byteArray, filename);
    }


    public Document createLetterPdf(StoredNotification storedNotification, String notificationReference) {
        byte[] decodedBytes = Base64.getDecoder().decode(storedNotification.getNotificationDocumentEncoded());
        ByteArrayResource byteArrayResource = getByteArrayResource(decodedBytes, notificationReference + ".PDF");
        return documentUploader.upload(byteArrayResource, PDF_CONTENT_TYPE);
    }

    private ByteArrayResource getByteArrayResource(byte[] byteArray, String filename) {
        return new ByteArrayResource(byteArray) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }

    private Document generatePdfForNotification(StoredNotification storedNotification) {
        if (storedNotification.getNotificationDocumentEncoded() != null) {
            return this.createLetterPdf(storedNotification, storedNotification.getNotificationReference());
        } else {
            return this.createPdf(storedNotification.getNotificationBody(), storedNotification.getNotificationReference());
        }
    }

    private static final Set<String> INVALID_NOTIFICATION_STATUSES = Set.of(
        "Cancelled",
        "Failed",
        "Technical-failure",
        "Temporary-failure",
        "Permanent-failure",
        "Validation-failed",
        "Virus-scan-failed"
    );

    private boolean shouldGeneratePdf(StoredNotification storedNotification) {
        return storedNotification.getNotificationDocument() == null
            && !INVALID_NOTIFICATION_STATUSES.contains(storedNotification.getNotificationStatus());
    }

    public List<IdValue<StoredNotification>> generatePdfsForNotifications(List<IdValue<StoredNotification>> existingNotifications) {
        existingNotifications.stream()
            .map(IdValue::getValue)
            .filter(this::shouldGeneratePdf)
            .forEach(notification ->
                notification.setNotificationDocument(generatePdfForNotification(notification)));
        return existingNotifications;
    }
}
