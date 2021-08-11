package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import java.net.MalformedURLException;
import java.net.URL;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.AccessTokenProvider;

@Service
public class DocumentDownloadClient {

    private final CaseDocumentClient caseDocumentClient;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final AccessTokenProvider accessTokenProvider;
    private final UserDetailsProvider userDetailsProvider;

    public DocumentDownloadClient(
        CaseDocumentClient caseDocumentClient,
        AuthTokenGenerator serviceAuthTokenGenerator,
        @Qualifier("requestUser") AccessTokenProvider accessTokenProvider,
        @Qualifier("requestUser") UserDetailsProvider userDetailsProvider
    ) {
        this.caseDocumentClient = caseDocumentClient;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.accessTokenProvider = accessTokenProvider;
        this.userDetailsProvider = userDetailsProvider;
    }

    public Resource download(String documentBinaryUrl) {

        URL url;

        try {
            url = new URL(documentBinaryUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid url for DocumentDownloadClientApi", e);
        }

        UserDetails userDetails = userDetailsProvider.getUserDetails();

        ResponseEntity<Resource> resourceResponseEntity = caseDocumentClient.getDocumentBinary(
            userDetails.getAccessToken(),
            serviceAuthTokenGenerator.generate(),
            url.toString());

        Resource documentResource = resourceResponseEntity.getBody();

        if (documentResource == null) {
            throw new IllegalStateException("Document could not be downloaded");
        }

        return documentResource;
    }
}
