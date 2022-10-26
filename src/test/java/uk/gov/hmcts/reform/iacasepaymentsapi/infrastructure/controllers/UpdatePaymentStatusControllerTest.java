package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.CaseMetaData;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentDto;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.CcdDataService;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.exceptions.BadRequestException;

@ExtendWith(MockitoExtension.class)
class UpdatePaymentStatusControllerTest {

    @Mock private CcdDataService ccdDataService;
    @Mock private PaymentDto paymentDto;

    private final String jurisdiction = "IA";
    private final String caseType = "Asylum";
    private final long caseId = 1234;

    private UpdatePaymentStatusController updatePaymentStatusController;

    @BeforeEach
    void setUp() {

        updatePaymentStatusController = new UpdatePaymentStatusController(ccdDataService);
    }

    @Test
    void should_update_the_payment_status_successfully() {

        when(paymentDto.getCcdCaseNumber()).thenReturn("1234");
        when(ccdDataService.updatePaymentStatus(any(CaseMetaData.class), eq(false)))
            .thenReturn(getSubmitEventResponse());

        ResponseEntity responseEntity = updatePaymentStatusController.updatePaymentStatus(paymentDto);

        SubmitEventDetails response = (SubmitEventDetails)responseEntity.getBody();

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(State.APPEAL_SUBMITTED, response.getState());
        assertEquals("RC-1234", response.getData().get("paymentReference"));
        assertEquals("Success", response.getData().get("paymentStatus"));
        assertEquals(200, response.getCallbackResponseStatusCode());
        assertEquals("CALLBACK_COMPLETED", response.getCallbackResponseStatus());
    }

    @Test
    void should_error_when_service_is_unavailable() {

        when(paymentDto.getCcdCaseNumber()).thenReturn("1234");
        when(ccdDataService.updatePaymentStatus(any(CaseMetaData.class), eq(false)))
            .thenThrow(ResponseStatusException.class);

        assertThatThrownBy(() -> updatePaymentStatusController.updatePaymentStatus(paymentDto))
            .isExactlyInstanceOf(ResponseStatusException.class);
    }

    @Test
    void should_error_on_invalid_ccd_case_number() {

        when(paymentDto.getCcdCaseNumber()).thenReturn("1001");
        when(ccdDataService.updatePaymentStatus(any(CaseMetaData.class), eq(false)))
            .thenThrow(BadRequestException.class);

        assertThatThrownBy(() -> updatePaymentStatusController.updatePaymentStatus(paymentDto))
            .isExactlyInstanceOf(BadRequestException.class);

    }

    private SubmitEventDetails getSubmitEventResponse() {

        Map<String, Object> data = new HashMap<>();
        data.put("appealReferenceNumber", "HU/50004/2021");
        data.put("paymentReference", "RC-1234");
        data.put("paymentStatus", "Success");

        return new SubmitEventDetails(1234, jurisdiction, State.APPEAL_SUBMITTED, data,
                                      200, "CALLBACK_COMPLETED");
    }
}
