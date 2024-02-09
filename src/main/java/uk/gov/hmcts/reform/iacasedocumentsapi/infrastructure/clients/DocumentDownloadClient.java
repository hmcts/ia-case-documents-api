package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;


@Service
@RequiredArgsConstructor
public class DocumentDownloadClient {



    private final FeatureToggler featureToggler;
    private final DmDocumentDownloadClient dmDocumentDownloadClient;
    private final CdamDocumentDownloadClient cdamDocumentDownLoadClient;


    public Resource download(String documentBinaryUrl) {
        return cdamDocumentDownLoadClient.download(documentBinaryUrl);
    }

}
