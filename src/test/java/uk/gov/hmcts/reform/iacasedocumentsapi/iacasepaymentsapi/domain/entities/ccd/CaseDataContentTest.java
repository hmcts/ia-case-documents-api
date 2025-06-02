package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class CaseDataContentTest {

    @Test
    void should_test_equals_contract() {

        EqualsVerifier.simple()
            .forClass(CaseDataContent.class)
            .verify();
    }

    @Test
    void should_hold_onto_values() {

        Map<String, Object> data = new HashMap<>();
        data.put("paymentStatus", "Success");
        data.put("paymentReference", "RC-1234");

        Map<String, Object> event = new HashMap<>();
        event.put("id", "updatePaymentStatus");

        boolean ignoreWarning = true;
        String eventToken = "eventToken";
        String caseReference = "1234";
        CaseDataContent caseDataContent = new CaseDataContent(caseReference, data, event, eventToken, ignoreWarning);

        assertEquals("1234", caseDataContent.getCaseReference());
        assertEquals(data, caseDataContent.getData());
        assertEquals("updatePaymentStatus", caseDataContent.getEvent().get("id"));
        assertEquals("eventToken", caseDataContent.getEventToken());
        assertTrue(caseDataContent.isIgnoreWarning());
    }
}
