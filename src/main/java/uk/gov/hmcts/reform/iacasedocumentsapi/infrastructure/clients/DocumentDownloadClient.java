package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;


@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentDownloadClient {



    private final FeatureToggler featureToggler;
    private final DmDocumentDownloadClient dmDocumentDownloadClient;
    private final CdamDocumentDownloadClient cdamDocumentDownLoadClient;


    public Resource download(String documentBinaryUrl) {
        if (featureToggler.getValue("use-ccd-document-am", false)) {
            // the below to be uncommented once downloadWithFallback will no longer be needed.
            //return cdamDocumentDownLoadClient.download(documentBinaryUrl);
            return downloadWithFallback(documentBinaryUrl);
        } else {
            return dmDocumentDownloadClient.download(documentBinaryUrl);
        }
    }

    /**
     * <p>
     *     Added on: 2023-08-30<br/>
     *     Incident: INC5541389<br/>
     *     Related ticket: RPMO-3511
     * </p>
     * <p>
     *     This is a fallback method to allow using the unsecured document store (dm) as opposed to the secured one
     *     (cdam) in case the secured one throws an HTTP-403.
     *     This method is not meant to be a permanent feature and it's meant to allow us to keep our feature flag
     *     turned on while ExUI waits for some work to be completed as well on their side.
     * </p>
     * <p>
     *     Once their work is completed, proceed as follows:
     *     1. add the use-ccd-document-am-fallback to production and switch it OFF
     *     2. give it a few weeks to ensure that all documents can be uploaded and downloaded without issues,
     *        monitoring the logs
     *     3. delete this method
     * </p>
     *
     * @param documentBinaryUrl
     * @return
     */
    private Resource downloadWithFallback(String documentBinaryUrl) {
        try {
            return cdamDocumentDownLoadClient.download(documentBinaryUrl);
        } catch (FeignException ex) {
            if (ex.status() == 403) {
                if (!featureToggler.getValue("use-ccd-document-am-fallback", true)) {
                    throw ex;
                }
                log.warn("A download using CDAM failed with an HTTP-403. This may be happening due to CDAM changes " +
                    "not being switched ON just yet in ExUI. A fallback will be used. Once confirmation is received " +
                    "that CDAM changes have been turned on in ExUI, create and switch OFF the " +
                    "use-ccd-document-am-fallback flag. Ticket reference: RPMO-3511.");
                return dmDocumentDownloadClient.download(documentBinaryUrl);
            } else {
                throw ex;
            }
        }
    }
}
