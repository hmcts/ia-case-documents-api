package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class SubmitEventDetailsTest {

    @Test
    void should_test_equals_contract() {

        EqualsVerifier.simple()
            .forClass(SubmitEventDetails.class)
            .verify();
    }

    @Test
    void should_hold_onto_values() {

        State state = State.APPEAL_SUBMITTED;
        Map<String, Object> data = new HashMap<>();
        data.put("paymentStatus", "Success");
        data.put("paymentReference", "RC-1234");

        String callbackResponseStatus = "CALLBACK_COMPLETED";
        int callbackResponseStatusCode = 200;
        String jurisdiction = "IA";
        long id = 1234;
        SubmitEventDetails submitEventDetails = new SubmitEventDetails(
            id, jurisdiction, state, data,
            callbackResponseStatusCode,
            callbackResponseStatus
        );

        assertEquals(id, submitEventDetails.getId());
        assertEquals(jurisdiction, submitEventDetails.getJurisdiction());
        assertEquals(State.APPEAL_SUBMITTED, submitEventDetails.getState());
        assertEquals(data, submitEventDetails.getData());
        assertEquals(callbackResponseStatusCode, submitEventDetails.getCallbackResponseStatusCode());
        assertEquals(callbackResponseStatus, submitEventDetails.getCallbackResponseStatus());
    }
}
