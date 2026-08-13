package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.StoredNotification;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
@Slf4j
public class SaveNotificationsToDataPdfService {

    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final Set<String> INVALID_NOTIFICATION_STATUSES = Set.of(
            "Cancelled",
            "Failed",
            "Technical-failure",
            "Temporary-failure",
            "Permanent-failure",
            "Validation-failed",
            "Virus-scan-failed"
    );
    private final DocumentToPdfConverter documentToPdfConverter;
    private final DocumentUploader documentUploader;
    private final DocumentHandler documentHandler;

    public SaveNotificationsToDataPdfService(
            DocumentUploader documentUploader,
            DocumentToPdfConverter documentToPdfConverter,
            DocumentHandler documentHandler) {
        this.documentUploader = documentUploader;
        this.documentToPdfConverter = documentToPdfConverter;
        this.documentHandler = documentHandler;
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

    public Document createLetterPdf(StoredNotification storedNotification, CaseData caseData) {
        byte[] decodedBytes = Base64.getDecoder().decode(storedNotification.getNotificationDocumentEncoded());
        LetterToDocumentType letterToDocumentType = getDocumentTypeFromNotificationReference(storedNotification.getNotificationReference());
        ByteArrayResource byteArrayResource = getByteArrayResource(decodedBytes, letterToDocumentType.getFileName() + ".PDF");
        Document document = documentUploader.upload(byteArrayResource, PDF_CONTENT_TYPE);
        if (letterToDocumentType.getDocumentTag().getCaseType().equals(CaseType.ASYLUM)) {
            documentHandler.addWithMetadata(
                    (AsylumCase) caseData,
                    document,
                    letterToDocumentType.getDocumentType(),
                    letterToDocumentType.getDocumentTag()
            );
            log.info("Uploaded notification document for notification reference: {}", "w");
        } else {
            throw new IllegalArgumentException("Unsupported case type for document tag: " + letterToDocumentType.getDocumentTag().getCaseType());
        }
        return document;
    }

    private ByteArrayResource getByteArrayResource(byte[] byteArray, String filename) {
        return new ByteArrayResource(byteArray) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }

    private Document generatePdfForNotification(StoredNotification storedNotification, CaseData caseData) {
        if (storedNotification.getNotificationDocumentEncoded() != null) {
            return this.createLetterPdf(storedNotification, caseData);
        } else {
            return this.createPdf(storedNotification.getNotificationBody(), storedNotification.getNotificationReference());
        }
    }

    private boolean shouldGeneratePdf(StoredNotification storedNotification) {
        return storedNotification.getNotificationDocument() == null
                && !INVALID_NOTIFICATION_STATUSES.contains(storedNotification.getNotificationStatus());
    }

    public List<IdValue<StoredNotification>> generatePdfsForNotifications(List<IdValue<StoredNotification>> existingNotifications, CaseData caseDate) {
        existingNotifications.stream()
                .map(IdValue::getValue)
                .filter(this::shouldGeneratePdf)
                .forEach(notification ->
                        notification.setNotificationDocument(generatePdfForNotification(notification, caseDate)));
        return existingNotifications;
    }

    private LetterToDocumentType getDocumentTypeFromNotificationReference(String notificationReference) {
        return Stream.of(LetterToDocumentType.values())
                .filter(letterToDocumentType -> notificationReference.contains(letterToDocumentType.name()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No matching document type found for notification reference: " + notificationReference));
    }

    @Getter
    private enum LetterToDocumentType {
        STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LETTER("24 weeks case review",
                AsylumCaseDefinition.TRIBUNAL_DOCUMENTS, DocumentTag.STF_24WEEKS_CASE_REVIEW_APPELLANT_DOCUMENT);

        private final String fileName;
        private final AsylumCaseDefinition documentType;
        private final DocumentTag documentTag;

        LetterToDocumentType(String fileName, AsylumCaseDefinition documentType, DocumentTag documentTag) {
            this.fileName = fileName;
            this.documentType = documentType;
            this.documentTag = documentTag;
        }
    }
}
