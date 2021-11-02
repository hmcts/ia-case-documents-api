package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.AccessTokenProvider;

@Service
public class DocumentDownloadClient {

    private final CaseDocumentClientApi caseDocumentClientApi;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final AccessTokenProvider accessTokenProvider;

    public DocumentDownloadClient(
        CaseDocumentClientApi caseDocumentClientApi,
        AuthTokenGenerator serviceAuthTokenGenerator,
        @Qualifier("requestUser") AccessTokenProvider accessTokenProvider
    ) {
        this.caseDocumentClientApi = caseDocumentClientApi;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.accessTokenProvider = accessTokenProvider;
    }

    public Resource download(String documentBinaryUrl) {

        URL url;

        try {
            url = new URL(documentBinaryUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid url for DocumentDownloadClientApi", e);
        }

        ResponseEntity<Resource> resourceResponseEntity = caseDocumentClientApi.getDocumentBinary(
            accessTokenProvider.getAccessToken(),
            serviceAuthTokenGenerator.generate(),
            UUID.fromString(url.getPath().split("/")[2]));

        Resource documentResource = resourceResponseEntity.getBody();

        if (documentResource == null) {
            throw new IllegalStateException("Document could not be downloaded");
        }

        return documentResource;
    }
}
