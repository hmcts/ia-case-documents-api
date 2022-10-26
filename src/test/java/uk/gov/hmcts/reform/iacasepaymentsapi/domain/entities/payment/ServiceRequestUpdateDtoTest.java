package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

public class ServiceRequestUpdateDtoTest {

    @Test
    void should_hold_onto_values() {
        String serviceRequestReference = "some-service-request-reference";
        String ccdCaseNumber = "some-ccd-case-number";
        String serviceRequestAmount = "some-service-request-amount";
        String serviceRequestStatus = "some-service-request-status";
        PaymentDto payment = mock(PaymentDto.class);

        ServiceRequestUpdateDto serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .serviceRequestReference(serviceRequestReference)
            .ccdCaseNumber(ccdCaseNumber)
            .serviceRequestAmount(serviceRequestAmount)
            .serviceRequestStatus(serviceRequestStatus)
            .payment(payment)
            .build();

        assertEquals("some-service-request-reference", serviceRequestUpdateDto.getServiceRequestReference());
        assertEquals("some-ccd-case-number", serviceRequestUpdateDto.getCcdCaseNumber());
        assertEquals("some-service-request-amount", serviceRequestUpdateDto.getServiceRequestAmount());
        assertEquals("some-service-request-status", serviceRequestUpdateDto.getServiceRequestStatus());
        assertEquals(payment, serviceRequestUpdateDto.getPayment());
    }
}
