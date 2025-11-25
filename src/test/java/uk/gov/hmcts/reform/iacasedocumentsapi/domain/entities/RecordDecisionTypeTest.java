package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Test;


public class RecordDecisionTypeTest {

    @Test
    public void check_all_options() {

        assertEquals("Refused", ApplicationDecision.REFUSED.toString());
        assertEquals("Granted", ApplicationDecision.GRANTED.toString());
        assertEquals("RefusedUnderIma", ApplicationDecision.REFUSED_UNDER_IMA.toString());
        assertEquals(3, ApplicationDecision.values().length);
    }
}
