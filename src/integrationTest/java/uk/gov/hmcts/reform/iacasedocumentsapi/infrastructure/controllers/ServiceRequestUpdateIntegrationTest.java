package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.IaCaseDocumentsApiClient.CALLBACK_COMPLETED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.IaCaseDocumentsApiClient.CCD_CASE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.IaCaseDocumentsApiClient.PAYMENT_AMOUNT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.IaCaseDocumentsApiClient.PAYMENT_CASE_REFERENCE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.IaCaseDocumentsApiClient.SUCCESS;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.IaCaseDocumentsApiClient;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.ServiceRequestUpdateDtoForTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithCcdStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithFeeStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithIdamStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithPaymentStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithServiceAuthStub;

@Slf4j
public class ServiceRequestUpdateIntegrationTest extends SpringBootIntegrationTest
    implements WithServiceAuthStub, WithFeeStub, WithPaymentStub, WithIdamStub, WithCcdStub {

    public static final String SERVICE_REQUEST_REFERENCE = "2020-0000000000000";

    private IaCaseDocumentsApiClient iaCaseDocumentsApiClient;

    @BeforeEach
    public void setup() {
        iaCaseDocumentsApiClient = new IaCaseDocumentsApiClient(objectMapper, mockMvc);
    }

    @Test
    public void serviceRequestUpdateEndpoint() throws Exception {
        addServiceAuthStub(server, "payment_app");
        addFeesRegisterStub(server);
        addPaymentStub(server);
        addUserInfoStub(server);
        addIdamTokenStub(server);
        addCcdUpdatePaymentStatusGetTokenStub(objectMapper, server);
        addCcdServiceRequestUpdateSubmitEventStub(objectMapper, server);

        SubmitEventDetails response = iaCaseDocumentsApiClient.serviceRequestUpdate(ServiceRequestUpdateDtoForTest.generateValid().build());

        assertNotNull(response);
        assertEquals(State.APPEAL_SUBMITTED, response.getState());
        assertEquals(SERVICE_REQUEST_REFERENCE, response.getData().get("service_request_reference"));
        assertEquals("paid", response.getData().get("service_request_status"));
        assertEquals(PAYMENT_AMOUNT.intValue(), response.getData().get("service_request_amount"));
        assertEquals(CCD_CASE_NUMBER, response.getData().get("ccd_case_number"));
        Map<String, Object> payment = new ObjectMapper().convertValue(response.getData().get("payment"),
                                                                      new TypeReference<>() {});
        assertEquals(SUCCESS, payment.get("status"));
        assertEquals(PAYMENT_CASE_REFERENCE, payment.get("payment_reference"));
        assertEquals(200, response.getCallbackResponseStatusCode());
        assertEquals(CALLBACK_COMPLETED, response.getCallbackResponseStatus());
    }

    @Test
    public void serviceRequestUpdateEndpoint_fails_if_wrong_service_name() throws Exception {
        addServiceAuthStub(server, "probate");
        addFeesRegisterStub(server);
        addPaymentStub(server);
        addUserInfoStub(server);
        addIdamTokenStub(server);
        addCcdUpdatePaymentStatusGetTokenStub(objectMapper, server);
        addCcdServiceRequestUpdateSubmitEventStub(objectMapper, server);

        HttpServletResponse response = iaCaseDocumentsApiClient.serviceRequestUpdateWithError(ServiceRequestUpdateDtoForTest.generateValid().build());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(403);
    }
}
