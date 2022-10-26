package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServiceRequestResponseTest {

    private String serviceRequestReference;

    @BeforeEach
    void setup() {
        serviceRequestReference = "some-service-request-reference";
    }

    @Test
    void should_hold_onto_values() {
        ServiceRequestResponse serviceRequestResponse = ServiceRequestResponse.builder()
            .serviceRequestReference(serviceRequestReference)
            .build();

        assertEquals("some-service-request-reference", serviceRequestResponse.getServiceRequestReference());
    }

    @Test
    void should_allow_all_args_constructor() {
        ServiceRequestResponse serviceRequestResponse = new ServiceRequestResponse(serviceRequestReference);

        assertEquals("some-service-request-reference", serviceRequestResponse.getServiceRequestReference());
    }

    @Test
    void should_allow_no_args_constructor() {
        ServiceRequestResponse serviceRequestResponse = new ServiceRequestResponse();

        assertNotNull(serviceRequestResponse);
    }
}
