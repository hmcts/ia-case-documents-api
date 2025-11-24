package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients;

import static java.lang.String.join;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.security.AccessTokenProvider;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;


@Service
public class DocumentDownloadSystemUserClient {

    private final DocumentDownloadClientApi documentDownloadClientApi;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final AccessTokenProvider accessTokenProvider;
    private final UserDetailsProvider userDetailsProvider;

    public DocumentDownloadSystemUserClient(
        DocumentDownloadClientApi documentDownloadClientApi,
        AuthTokenGenerator serviceAuthTokenGenerator,
        @Qualifier("systemUser") AccessTokenProvider accessTokenProvider,
        @Qualifier("systemUser") UserDetailsProvider userDetailsProvider
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

    public JSONObject getJsonObjectFromDocument(DocumentWithMetadata document) throws IOException, NotificationClientException {
        Resource resource =
                download(document.getDocument().getDocumentBinaryUrl());
        return NotificationClient.prepareUpload(resource.getInputStream().readAllBytes());
    }
}

