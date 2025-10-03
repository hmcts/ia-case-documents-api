package uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.payment.PaymentDto;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.CallbackForTest;

@Slf4j
public class IaCaseDocumentsApiClient {

    private static final String SERVICE_JWT_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    private static final String USER_JWT_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDVVaTlXVGlvTHQwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJjY2QtaW1wb3J0QGZha2UuaG1jdHMubmV0IiwiYXV0aF9sZXZlbCI6MCwiYXVkaXRUcmFja2luZ0lkIjoiZDg3ODI3ODQtMWU0NC00NjkyLTg0NzgtNTI5MzE0NTVhNGI5IiwiaXNzIjoiaHR0cDovL2ZyLWFtOjgwODAvb3BlbmFtL29hdXRoMi9obWN0cyIsInRva2VuTmFtZSI6ImFjY2Vzc190b2tlbiIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJhdXRoR3JhbnRJZCI6IjNjMWMzNjFkLTRlYzUtNGY0NS1iYzI0LTUxOGMzMDk0MzUxYiIsImF1ZCI6ImNjZF9nYXRld2F5IiwibmJmIjoxNTg0NTI2MzcyLCJncmFudF90eXBlIjoicGFzc3dvcmQiLCJzY29wZSI6WyJvcGVuaWQiLCJwcm9maWxlIiwicm9sZXMiXSwiYXV0aF90aW1lIjoxNTg0NTI2MzcyLCJyZWFsbSI6Ii9obWN0cyIsImV4cCI6MTU4NDU1NTE3MiwiaWF0IjoxNTg0NTI2MzcyLCJleHBpcmVzX2luIjoyODgwMCwianRpIjoiNDhjNDMzYTQtZmRiNS00YTIwLWFmNGUtMmYyNjIyYjYzZmU5In0.WP8ATcHMmdtG2W443aqNz3ES6-Bqng0IKjTQfbndN1HrBLJWJtpC3qfzy2wD_CdiPU4uspdN5S91nhiT8Ub6DjstnDz3VPmR3Cbdk5QJBdAsQ0ah9w-duS8SA_dlzDIMt18bSDMUUdck6YxsoNyQFisI6cKNnfgB9ZTLhenVENtdmyrKVr96Ezp-jhhzmMVMxb1rW7KghSAH0ZCsWqlhrM--jPGRCweDiFe-ldi4EuhIxGbkPjyWwsdcgmYfIuFrSxqV0vrSI37DNZx_Sh5DVJpUgSrYKRzuMqe4rFN6WVyHIY_Qu52ER2yrNYtGbAQ5AyMabPTPj9VVxqpa5nYUAg";
    public static final String ID = "1234";
    public static final String PAYMENT_CASE_REFERENCE = "RC-1627-5070-9329-7815";
    public static final String CCD_CASE_NUMBER = "1627506765384547";
    public static final String JURISDICTION = "IA";
    public static final BigDecimal PAYMENT_AMOUNT = BigDecimal.valueOf(140);
    public static final String CALLBACK_COMPLETED = "CALLBACK_COMPLETED";
    public static final String SUCCESS = "success";
    public static final String APPEAL_REFERENCE_NUMBER = "HU/50004/2021";

    private final String aboutToSubmitUrl;
    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;
    private final String aboutToStartUrl;
    private final String ccdSubmittedUrl;
    private final String updatePaymentStatusUrl;
    private final String serviceRequestUpdateUrl;
    private final HttpHeaders httpHeaders = new HttpHeaders();

    public IaCaseDocumentsApiClient(ObjectMapper objectMapper, MockMvc mockMvc) {
        this.objectMapper = objectMapper;
        this.mockMvc = mockMvc;
        this.aboutToSubmitUrl = "/asylum/ccdAboutToSubmit";
        this.aboutToStartUrl = "/asylum/ccdAboutToStart";
        this.ccdSubmittedUrl = "/asylum/ccdSubmitted";
        this.updatePaymentStatusUrl = "/payment-updates";
        this.serviceRequestUpdateUrl = "/service-request-update";

        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        httpHeaders.add("Authorization", USER_JWT_TOKEN);
        httpHeaders.add("ServiceAuthorization", SERVICE_JWT_TOKEN);
    }

    public PreSubmitCallbackResponseForTest aboutToSubmit(CallbackForTestBuilder callback) {
        MvcResult response = null;
        try {
            response = mockMvc
                .perform(
                    post(aboutToSubmitUrl)
                        .headers(httpHeaders)
                        .content(toJson(callback.build()))
                        .contentType(APPLICATION_JSON_VALUE)
                ).andReturn();
            return objectMapper.readValue(
                response.getResponse().getContentAsString(),
                PreSubmitCallbackResponseForTest.class
            );
        } catch (Exception e) {
            String message = e.getMessage();
            if (response != null && response.getResponse().getStatus() != 200) {
                try {
                    message = response.getResponse().getContentAsString() + " " + response.getResponse().getStatus();
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                    // test will fail
                    throw new RuntimeException(ex);
                }
            }
            throw new RuntimeException(message, e);
        }
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
            .readValue(response.getContentAsByteArray(), uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest.class));
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
            .readValue(response.getContentAsByteArray(), uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest.class));
    }

    public SubmitEventDetails updatePaymentStatus(PaymentDto paymentDto) throws Exception {
        final MockHttpServletResponse response =
            mockMvc.perform(put(updatePaymentStatusUrl)
                                .headers(httpHeaders)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(paymentDto)))
                .andExpect(status().isOk()).andReturn().getResponse();

        return translateException(() -> objectMapper
            .readValue(response.getContentAsByteArray(), SubmitEventDetails.class));
    }

    public HttpServletResponse updatePaymentStatusWithError(PaymentDto paymentDto) throws Exception {
        return mockMvc.perform(put(updatePaymentStatusUrl)
                                   .headers(httpHeaders)
                                   .contentType(MediaType.APPLICATION_JSON)
                                   .content(toJson(paymentDto)))
            .andReturn().getResponse();
    }

    public SubmitEventDetails serviceRequestUpdate(ServiceRequestUpdateDto serviceRequestUpdateDto) throws Exception {
        final MockHttpServletResponse response =
            mockMvc.perform(put(serviceRequestUpdateUrl)
                                .headers(httpHeaders)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(serviceRequestUpdateDto)))
                .andExpect(status().isOk()).andReturn().getResponse();

        return translateException(() -> objectMapper
            .readValue(response.getContentAsByteArray(), SubmitEventDetails.class));
    }

    public HttpServletResponse serviceRequestUpdateWithError(ServiceRequestUpdateDto serviceRequestUpdateDto) throws Exception {
        return mockMvc.perform(put(serviceRequestUpdateUrl)
                                   .headers(httpHeaders)
                                   .contentType(MediaType.APPLICATION_JSON)
                                   .content(toJson(serviceRequestUpdateDto)))
            .andReturn().getResponse();
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
