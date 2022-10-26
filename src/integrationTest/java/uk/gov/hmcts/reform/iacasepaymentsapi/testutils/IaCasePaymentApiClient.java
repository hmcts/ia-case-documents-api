package uk.gov.hmcts.reform.iacasepaymentsapi.testutils;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import groovy.util.logging.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

@Slf4j
public class IaCasePaymentApiClient {

    private final MockMvc mockMvc;
    private final String aboutToSubmitUrl;
    private final String aboutToStartUrl;
    private final String ccdSubmittedUrl;

    private final HttpHeaders httpHeaders = new HttpHeaders();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public IaCasePaymentApiClient(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        this.aboutToSubmitUrl = "/asylum/ccdAboutToSubmit";
        this.aboutToStartUrl = "/asylum/ccdAboutToStart";
        this.ccdSubmittedUrl = "/asylum/ccdSubmitted";

        objectMapper.registerModule(new JavaTimeModule());

        httpHeaders.add(AUTHORIZATION, "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiMWVyMFdSd2dJT1RBRm9q"
                                       + "RTRyQy9mYmVLdTNJPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJpYS5sZWdhbHJlcC5hLmNjZE"
                                       + "BnbWFpbC5jb20iLCJjdHMiOiJPQVVUSDJfU1RBVEVMRVNTX0dSQU5UIiwiYXV0aF9sZXZlbCI6"
                                       + "MCwiYXVkaXRUcmFja2luZ0lkIjoiYjAxMjYxMzgtYWUwMC00ZmQ5LWFlNGEtZWQyNjUyODExMW"
                                       + "E4LTI1MDE1MTY3IiwiaXNzIjoiaHR0cHM6Ly9mb3JnZXJvY2stYW0uc2VydmljZS5jb3JlLWNv"
                                       + "bXB1dGUtaWRhbS1hYXQyLmludGVybmFsOjg0NDMvb3BlbmFtL29hdXRoMi9yZWFsbXMvcm9vdC"
                                       + "9yZWFsbXMvaG1jdHMiLCJ0b2tlbk5hbWUiOiJhY2Nlc3NfdG9rZW4iLCJ0b2tlbl90eXBlIjoi"
                                       + "QmVhcmVyIiwiYXV0aEdyYW50SWQiOiJwTExqdmNQWWl1TWFXd3hrbFpXUzJNV3FaZVEiLCJhdW"
                                       + "QiOiJ4dWl3ZWJhcHAiLCJuYmYiOjE1OTA2NzQ0ODYsImdyYW50X3R5cGUiOiJhdXRob3JpemF0"
                                       + "aW9uX2NvZGUiLCJzY29wZSI6WyJvcGVuaWQiLCJwcm9maWxlIiwicm9sZXMiLCJjcmVhdGUtdX"
                                       + "NlciIsIm1hbmFnZS11c2VyIl0sImF1dGhfdGltZSI6MTU5MDY3NDQ4NiwicmVhbG0iOiIvaG1j"
                                       + "dHMiLCJleHAiOjE1OTA3MDMyODYsImlhdCI6MTU5MDY3NDQ4NiwiZXhwaXJlc19pbiI6Mjg4MD"
                                       + "AsImp0aSI6Ik05d2t1TEhMWnk3SEpLWWk5ZDBOOFdfcjEtMCJ9.ed7TiNviY0HPRNVu80M0Xe7"
                                       + "2am_2HXeJ_2LUxWM4sXOAktqqyShFGmwC-NcMEgsRvASFT3DUaAgpuOZkypKtv2Lw3T7NJigfV"
                                       + "kiod3y2G-3BeRq-0otx7gm4-D6UBmJ7vKuct5I9qZsAjYYCb8vMQQAU-sAnD6RfPeqA8kLxuDK"
                                       + "vePrj_7ntEap-s-2jcLVLf4W2MU6VaZFK4fs99daRtn6eHQWU0PwirIxa0miuzu3AenyMi43K9"
                                       + "NemUma5lnIn_UU-_AxytfYoFBuVw5eKtmjEPW6H1foxjFqbM8ovKEhD7Iod7Rn1R6mQMhZpesk"
                                       + "pQkutWvaViejNfM-lhO-eaQ");
        httpHeaders.add(SERVICE_AUTHORIZATION, "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJmcGxfY2FzZV9zZXJ2aWNlIiwi"
                                               + "ZXhwIjoxNTkwNjg4NTU4fQ.I-Kuj5uEQqInvIn53bBoJtbtEYp5BGK-qakBjU5V_Mz"
                                               + "ruwmGq9ctPo6kYhnSkzvQ81sasaHZXtjbSKWfnUNeZg");
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
        objectMapper.registerModule(new JavaTimeModule());
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
