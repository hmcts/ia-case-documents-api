package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;

import java.io.File;
import java.io.IOException;

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

        ByteArrayResource byteArrayResource = getByteArrayResource(
            notificationPdf,
            notificationReference
            );

        return documentUploader.upload(byteArrayResource, PDF_CONTENT_TYPE);
    }

    private ByteArrayResource getByteArrayResource(File notificationPdf, String filename) {

        byte[] byteArray;

        try {
            byteArray = FileUtils.readFileToByteArray(notificationPdf);

        } catch (IOException e) {
            throw new IllegalStateException("Error reading converted pdf");
        }

        return new ByteArrayResource(byteArray) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }
}
