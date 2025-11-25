package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import java.io.IOException;
import org.json.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentDownloadClient {

    private final FeatureToggler featureToggler;
    private final DmDocumentDownloadClient dmDocumentDownloadClient;
    private final CdamDocumentDownloadClient cdamDocumentDownLoadClient;

    public Resource download(String documentBinaryUrl) {
        if (featureToggler.getValue("use-ccd-document-am", false)) {
            log.info("Downloading {} using CDAM", documentBinaryUrl);
            return cdamDocumentDownLoadClient.download(documentBinaryUrl);
        } else {
            log.info("Downloading {} not using CDAM", documentBinaryUrl);
            return dmDocumentDownloadClient.download(documentBinaryUrl);
        }
    }

    public JSONObject getJsonObjectFromDocument(DocumentWithMetadata document) throws IOException, NotificationClientException {
        Resource resource =
            download(document.getDocument().getDocumentBinaryUrl());
        return NotificationClient.prepareUpload(resource.getInputStream().readAllBytes());
    }

}
