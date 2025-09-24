package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities;

import static junit.framework.TestCase.assertEquals;

import org.junit.jupiter.api.Test;

class ValueTest {

    private final String code = "code123";
    private final String label = "PBA1234567";
    private Value account = new Value(code, label);

    @Test
    void should_hold_onto_values() {
        assertEquals(code, account.getCode());
        assertEquals(label, account.getLabel());
    }
}
