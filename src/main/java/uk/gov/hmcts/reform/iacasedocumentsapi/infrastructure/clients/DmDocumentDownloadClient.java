package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import static java.lang.String.join;

import java.net.MalformedURLException;
import java.net.URL;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.AccessTokenProvider;

/**
 * Superseded. Will need to be removed as soon as the "use-ccd-document-am" feature flag is permanently on
 */
@Component
@Deprecated
@Slf4j
public class DmDocumentDownloadClient {

    private final DocumentDownloadClientApi documentDownloadClientApi;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final AccessTokenProvider accessTokenProvider;
    private final UserDetailsProvider userDetailsProvider;

    public DmDocumentDownloadClient(
        DocumentDownloadClientApi documentDownloadClientApi,
        AuthTokenGenerator serviceAuthTokenGenerator,
        AccessTokenProvider accessTokenProvider,
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

        String serviceAuth = serviceAuthTokenGenerator.generate();

        log.info(
            "Downloading document: userAuth {}, serviceAuth {}, userId {}, uri {}, binaryUri {}",
            accessTokenProvider.getAccessToken(),
            serviceAuth,
            userDetails.getId(),
            url.getPath().substring(1),
            documentBinaryUrl
        );

        ResponseEntity<Resource> resourceResponseEntity = documentDownloadClientApi.downloadBinary(
            accessTokenProvider.getAccessToken(),
            serviceAuth,
            join(",", userDetails.getRoles()),
            userDetails.getId(),
            url.getPath().substring(1)
        );

        Resource documentResource = resourceResponseEntity.getBody();

        if (documentResource == null) {
            throw new IllegalStateException("Document could not be downloaded");
        }

        return documentResource;
    }
}
