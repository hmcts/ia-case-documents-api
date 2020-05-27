package uk.gov.hmcts.reform.iacasepaymentsapi.component.testutils;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.util.logging.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class IaCasePaymentsApiClient {

    private final MockMvc mockMvc;
    private final RestTemplate restTemplate;
    private final String aboutToSubmitUrl;
    private final String aboutToStartUrl;
    private final String ccdSubmittedUrl;

    private final HttpHeaders httpHeaders = new HttpHeaders();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public IaCasePaymentsApiClient(MockMvc mockMvc, int port) {
        this.mockMvc = mockMvc;
        restTemplate = new RestTemplate();
        this.aboutToSubmitUrl = "http://localhost:" + port + "/asylum/ccdAboutToSubmit";
        this.aboutToStartUrl = "http://localhost:" + port + "/asylum/ccdAboutToStart";
        this.ccdSubmittedUrl = "http://localhost:" + port + "/asylum/ccdSubmitted";
    }

    public PreSubmitCallbackResponseForTest aboutToSubmit(
        CallbackForTest.CallbackForTestBuilder callback
    ) throws Exception {

        final MockHttpServletResponse response = mockMvc.perform(post(aboutToSubmitUrl)
            .headers(httpHeaders)
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(callback.build())))
            .andExpect(status().isOk()).andReturn().getResponse();

        return translateException(() -> objectMapper
            .readValue(response.getContentAsByteArray(), PreSubmitCallbackResponseForTest.class));
    }

    public PreSubmitCallbackResponseForTest aboutToStart(
        CallbackForTest.CallbackForTestBuilder callback
    ) throws Exception {

        final MockHttpServletResponse response = mockMvc.perform(post(aboutToStartUrl)
            .headers(httpHeaders)
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(callback.build())))
            .andReturn().getResponse();

        return translateException(() -> objectMapper
            .readValue(response.getContentAsByteArray(), PreSubmitCallbackResponseForTest.class));
    }

    public PreSubmitCallbackResponseForTest ccdSubmitted(
        CallbackForTest.CallbackForTestBuilder callback
    ) throws Exception {

        final MockHttpServletResponse response = mockMvc.perform(post(ccdSubmittedUrl)
            .headers(httpHeaders)
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(callback.build())))
            .andExpect(status().isOk()).andReturn().getResponse();

        return translateException(() -> objectMapper
            .readValue(response.getContentAsByteArray(), PreSubmitCallbackResponseForTest.class));
    }

    private String toJson(Object o) {
        return translateException(() -> objectMapper.writeValueAsString(o));
    }

    private <T> T translateException(CallableWithException<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    interface CallableWithException<T> {
        T call() throws Exception;
    }
}
