package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import static java.lang.String.join;

import java.net.MalformedURLException;
import java.net.URL;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.AccessTokenProvider;

@Service
public class DocumentDownloadClient {

    private final DocumentDownloadClientApi documentDownloadClientApi;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final AccessTokenProvider accessTokenProvider;
    private final UserDetailsProvider userDetailsProvider;

    public DocumentDownloadClient(
        DocumentDownloadClientApi documentDownloadClientApi,
        AuthTokenGenerator serviceAuthTokenGenerator,
        @Qualifier("requestUser") AccessTokenProvider accessTokenProvider,
        @Qualifier("requestUser") UserDetailsProvider userDetailsProvider
    ) {
        this.documentDownloadClientApi = documentDownloadClientApi;
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

        ResponseEntity<Resource> resourceResponseEntity = documentDownloadClientApi.downloadBinary(
            accessTokenProvider.getAccessToken(),
            serviceAuthTokenGenerator.generate(),
            join(",", userDetails.getRoles()),
            userDetails.getId(),
            url.getPath().substring(1));

        Resource documentResource = resourceResponseEntity.getBody();

        if (documentResource == null) {
            throw new IllegalStateException("Document could not be downloaded");
        }

        return documentResource;
    }
}
