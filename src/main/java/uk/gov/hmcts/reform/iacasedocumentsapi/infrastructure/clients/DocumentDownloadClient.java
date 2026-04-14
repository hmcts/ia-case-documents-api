package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentDownloadClient {

    private final CdamDocumentDownloadClient cdamDocumentDownLoadClient;

    public Resource download(String documentBinaryUrl) {
        log.info("Downloading {} using CDAM", documentBinaryUrl);
        return cdamDocumentDownLoadClient.download(documentBinaryUrl);
    }

}
