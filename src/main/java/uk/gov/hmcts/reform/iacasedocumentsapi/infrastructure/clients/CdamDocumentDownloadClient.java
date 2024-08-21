package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.AccessTokenProvider;

@Component
@Slf4j
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

    //TODO Upgrade ccd-case-document-am-client. Need to get UUID, string cannot be converted.
    public Resource download(String documentBinaryUrl) {
        log.info("Downloading {} using CaseDocumentClient (CDAM)", documentBinaryUrl);
        ResponseEntity<Resource> resourceResponseEntity = caseDocumentClient.getDocumentBinary(
            accessTokenProvider.getAccessToken(),
            serviceAuthTokenGenerator.generate(),
            documentBinaryUrl
        );
        Resource documentResource = resourceResponseEntity.getBody();
        if (documentResource == null) {
            throw new IllegalStateException("Document could not be downloaded");
        }
        return documentResource;
    }
}
