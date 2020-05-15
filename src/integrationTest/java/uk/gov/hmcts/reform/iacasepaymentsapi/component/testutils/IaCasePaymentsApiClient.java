package uk.gov.hmcts.reform.iacasepaymentsapi.component.testutils;

import groovy.util.logging.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.fixtures.CallbackForTest;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.fixtures.PreSubmitCallbackResponseForTest;

@Slf4j
public class IaCasePaymentsApiClient {

    private final RestTemplate restTemplate;
    private final String aboutToSubmitUrl;
    private final String aboutToStartUrl;
    private final String ccdSubmittedUrl;

    public IaCasePaymentsApiClient(int port) {
        restTemplate = new RestTemplate();
        this.aboutToSubmitUrl = "http://localhost:" + port + "/asylum/ccdAboutToSubmit";
        this.aboutToStartUrl = "http://localhost:" + port + "/asylum/ccdAboutToStart";
        this.ccdSubmittedUrl = "http://localhost:" + port + "/asylum/ccdSubmitted";
    }

    public PreSubmitCallbackResponseForTest aboutToSubmit(CallbackForTest.CallbackForTestBuilder callback) {

        HttpEntity<CallbackForTest> requestEntity =
            new HttpEntity<>(callback.build(), getHeaders());

        ResponseEntity<PreSubmitCallbackResponseForTest> responseEntity =
            restTemplate.exchange(
                aboutToSubmitUrl,
                HttpMethod.POST,
                requestEntity,
                PreSubmitCallbackResponseForTest.class
            );

        return responseEntity.getBody();
    }

    public PreSubmitCallbackResponseForTest aboutToStart(CallbackForTest.CallbackForTestBuilder callback) {

        HttpEntity<CallbackForTest> requestEntity =
            new HttpEntity<>(callback.build(), getHeaders());

        ResponseEntity<PreSubmitCallbackResponseForTest> responseEntity =
            restTemplate.exchange(
                aboutToStartUrl,
                HttpMethod.POST,
                requestEntity,
                PreSubmitCallbackResponseForTest.class
            );

        return responseEntity.getBody();
    }

    public PreSubmitCallbackResponseForTest ccdSubmitted(CallbackForTest.CallbackForTestBuilder callback) {

        HttpEntity<CallbackForTest> requestEntity =
            new HttpEntity<>(callback.build(), getHeaders());

        ResponseEntity<PreSubmitCallbackResponseForTest> responseEntity =
            restTemplate.exchange(
                ccdSubmittedUrl,
                HttpMethod.POST,
                requestEntity,
                PreSubmitCallbackResponseForTest.class
            );

        return responseEntity.getBody();
    }


    private HttpHeaders getHeaders() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return headers;
    }
}
