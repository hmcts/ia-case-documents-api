package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.AccessTokenProvider;

@Slf4j
@Component
public class CdamDocumentDownloadClient {
    private final CaseDocumentClient caseDocumentClient;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final AccessTokenProvider accessTokenProvider;

    public CdamDocumentDownloadClient(
        CaseDocumentClient caseDocumentClient,
        AuthTokenGenerator serviceAuthTokenGenerator,
        AccessTokenProvider accessTokenProvider
    ) {

        this.caseDocumentClient = caseDocumentClient;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.accessTokenProvider = accessTokenProvider;
    }

    public Resource download(String documentBinaryUrl) {
        String accessToken = accessTokenProvider.getAccessToken();
        String serviceAuthToken = serviceAuthTokenGenerator.generate();
        log.info("5", accessToken);
        log.info("6", serviceAuthToken);
        log.info("7", documentBinaryUrl);
        ResponseEntity<Resource> resourceResponseEntity = caseDocumentClient.getDocumentBinary(
            accessToken,
            serviceAuthToken,
            documentBinaryUrl
        );
        Resource documentResource = resourceResponseEntity.getBody();
        if (documentResource == null) {
            throw new IllegalStateException("Document could not be downloaded");
        }
        return documentResource;
    }
}
