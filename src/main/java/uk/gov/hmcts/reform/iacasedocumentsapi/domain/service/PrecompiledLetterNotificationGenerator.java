package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LETTER_BUNDLE_DOCUMENTS;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.core.io.Resource;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;

public class PrecompiledLetterNotificationGenerator implements NotificationGenerator {

    protected final List<DocumentTag> documentTagList;
    protected final NotificationIdAppender notificationIdAppender;
    protected final GovNotifyNotificationSender notificationSender;
    protected final DocumentDownloadClient documentDownloadClient;

    public PrecompiledLetterNotificationGenerator(
        List<DocumentTag> documentTagList,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender,
        DocumentDownloadClient documentDownloadClient) {
        this.documentTagList = documentTagList;
        this.notificationSender = notificationSender;
        this.notificationIdAppender = notificationIdAppender;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public void generate(Callback<AsylumCase> callback) {

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        documentTagList.forEach(documentTag -> {
            String caseId = String.valueOf(callback.getCaseDetails().getId());
            String referenceId = caseId + "_" + documentTag.name();
            List<String> notificationIds;
            try {
                notificationIds = createPrecompiledLetter(documentTag, asylumCase, referenceId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            notificationIdAppender.appendAll(asylumCase, referenceId, notificationIds);
        });
    }

    protected List<String> createPrecompiledLetter(
        final DocumentTag documentTag,
        final AsylumCase asylumCase,
        final String referenceId) throws IOException {
        Optional<List<IdValue<DocumentWithMetadata>>> optionalLetterNotificationDocs = asylumCase.read(LETTER_BUNDLE_DOCUMENTS);

        DocumentWithMetadata bundledLetterPdf = optionalLetterNotificationDocs
            .orElse(Collections.emptyList())
            .stream()
            .map(IdValue::getValue)
            .filter(d -> d.getTag() == documentTag)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(documentTag + " document not available"));

        Resource resource =
            documentDownloadClient.download(bundledLetterPdf.getDocument().getDocumentBinaryUrl());


        return Arrays.asList(
            sendPrecompiledLetter(
                referenceId,
                resource.getInputStream()));
    }

    protected String sendPrecompiledLetter(
        final String referenceId,
        final InputStream inputStream) throws IOException {

        return notificationSender.sendPrecompiledLetter(
            referenceId,
            inputStream
        );
    }



}
