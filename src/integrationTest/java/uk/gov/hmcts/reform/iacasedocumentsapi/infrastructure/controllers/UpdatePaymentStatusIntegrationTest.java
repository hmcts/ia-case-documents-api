package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.IaCasePaymentApiClient;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.fixtures.PaymentDtoForTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.WithCcdStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.WithFeeStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.WithIdamStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.WithPaymentStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.WithServiceAuthStub;

import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State.APPEAL_SUBMITTED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.utilities.IaCasePaymentApiClient.CALLBACK_COMPLETED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.utilities.IaCasePaymentApiClient.CCD_CASE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.utilities.IaCasePaymentApiClient.ID;
import static uk.gov.hmcts.reform.iacasedocumentsapi.utilities.IaCasePaymentApiClient.JURISDICTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.utilities.IaCasePaymentApiClient.SUCCESS;

@Slf4j
public class UpdatePaymentStatusIntegrationTest extends SpringBootIntegrationTest
    implements WithServiceAuthStub, WithFeeStub, WithPaymentStub, WithIdamStub, WithCcdStub {

    private IaCasePaymentApiClient iaCasePaymentApiClient;

    @BeforeEach
    public void setup() {
        iaCasePaymentApiClient = new IaCasePaymentApiClient(mockMvc);
    }

    @Test
    public void updatePaymentStatusEndpoint() throws Exception {
        addServiceAuthStub(server, "payment_app");
        addFeesRegisterStub(server);
        addPaymentStub(server);
        addUserInfoStub(server);
        addIdamTokenStub(server);
        addCcdUpdatePaymentStatusGetTokenStub(server);
        addCcdUpdatePaymentSubmitEventStub(server);

        SubmitEventDetails response = iaCasePaymentApiClient.updatePaymentStatus(PaymentDtoForTest.generateValid().build());

        assertNotNull(response);
        assertEquals(200, response.getCallbackResponseStatusCode());
        assertEquals(APPEAL_SUBMITTED, response.getState());
        assertEquals(CALLBACK_COMPLETED, response.getCallbackResponseStatus());
        assertEquals(IaCasePaymentApiClient.APPEAL_REFERENCE_NUMBER,
                     response.getData().get(APPEAL_REFERENCE_NUMBER.value()));
        assertEquals(CCD_CASE_NUMBER, response.getData().get(PAYMENT_REFERENCE.value()));
        assertEquals(SUCCESS, response.getData().get(PAYMENT_STATUS.value()));
        assertEquals(Long.parseLong(ID), response.getId());
        assertEquals(JURISDICTION, response.getJurisdiction());
        assertEquals(APPEAL_SUBMITTED, response.getState());
    }

    @Test
    public void updatePaymentStatusEndpoint_fails_if_wrong_service_name() throws Exception {
        addServiceAuthStub(server, "probate");
        addFeesRegisterStub(server);
        addPaymentStub(server);
        addUserInfoStub(server);
        addIdamTokenStub(server);
        addCcdUpdatePaymentStatusGetTokenStub(server);
        addCcdUpdatePaymentSubmitEventStub(server);

        HttpServletResponse response = iaCasePaymentApiClient.updatePaymentStatusWithError(PaymentDtoForTest.generateValid().build());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(403);
    }
}
