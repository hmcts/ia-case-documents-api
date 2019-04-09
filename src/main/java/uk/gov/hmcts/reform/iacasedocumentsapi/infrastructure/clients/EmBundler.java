package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentBundler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.Bundle;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.BundleCaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.BundleDocument;
import uk.gov.hmcts.reform.logging.exception.AlertLevel;

@Service
public class EmBundler implements DocumentBundler {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final String emBundlerUrl;
    private final String emBundlerStitchUri;
    private final RestTemplate restTemplate;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final UserDetailsProvider userDetailsProvider;
    private final DateProvider dateProvider;

    public EmBundler(
        @Value("${emBundler.url}") String emBundlerUrl,
        @Value("${emBundler.stitch.uri}") String emBundlerStitchUri,
        RestTemplate restTemplate,
        AuthTokenGenerator serviceAuthTokenGenerator,
        @Qualifier("requestUser") UserDetailsProvider userDetailsProvider,
        DateProvider dateProvider
    ) {
        this.emBundlerUrl = emBundlerUrl;
        this.emBundlerStitchUri = emBundlerStitchUri;
        this.restTemplate = restTemplate;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.userDetailsProvider = userDetailsProvider;
        this.dateProvider = dateProvider;
    }

    public Document bundle(
        List<DocumentWithMetadata> documents,
        String bundleTitle,
        String bundleFilename
    ) {
        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        final UserDetails userDetails = userDetailsProvider.getUserDetails();
        final String accessToken = userDetails.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, accessToken);
        headers.set(SERVICE_AUTHORIZATION, serviceAuthorizationToken);

        List<IdValue<BundleDocument>> bundleDocuments = new ArrayList<>();

        for (int i = 0; i < documents.size(); i++) {

            DocumentWithMetadata caseDocument = documents.get(i);

            bundleDocuments.add(
                new IdValue<>(
                    String.valueOf(i),
                    new BundleDocument(
                        caseDocument.getDocument().getDocumentFilename(),
                        caseDocument.getDescription(),
                        i,
                        caseDocument.getDocument()
                    )
                )
            );
        }

        Callback<BundleCaseData> payload =
            new Callback<>(
                new CaseDetails<>(
                    1L,
                    "IA",
                    State.UNKNOWN,
                    new BundleCaseData(
                        Collections.singletonList(
                            new IdValue<>(
                                "1",
                                new Bundle(
                                    "1",
                                    bundleTitle,
                                    "",
                                    "yes",
                                    bundleDocuments
                                )
                            )
                        )
                    ),
                    dateProvider.nowWithTime()
                ),
                Optional.empty(),
                Event.GENERATE_HEARING_BUNDLE
            );

        HttpEntity<Callback<BundleCaseData>> requestEntity = new HttpEntity<>(payload, headers);

        PreSubmitCallbackResponse<BundleCaseData> response;

        try {

            response =
                restTemplate
                    .exchange(
                        emBundlerUrl + emBundlerStitchUri,
                        HttpMethod.POST,
                        requestEntity,
                        new ParameterizedTypeReference<PreSubmitCallbackResponse<BundleCaseData>>() {
                        }
                    ).getBody();

            Document bundle =
                response
                    .getData()
                    .getCaseBundles()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new DocumentServiceResponseException(AlertLevel.P2, "Bundle was not created"))
                    .getValue()
                    .getStitchedDocument()
                    .orElseThrow(() -> new DocumentServiceResponseException(AlertLevel.P2, "Bundle was not created"));

            Document renamedBundle = new Document(
                bundle.getDocumentUrl(),
                bundle.getDocumentBinaryUrl(),
                bundleFilename
            );

            return renamedBundle;

        } catch (RestClientException e) {

            throw new DocumentServiceResponseException(
                AlertLevel.P2,
                "Couldn't create bundle using API: " + emBundlerUrl + emBundlerStitchUri,
                e
            );
        }
    }
}
