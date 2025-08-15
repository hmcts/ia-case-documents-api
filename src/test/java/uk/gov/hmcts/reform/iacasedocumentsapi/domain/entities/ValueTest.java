package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ValueTest {

    private final String code = "code123";
    private final String label = "PBA1234567";
    private final Value account = new Value(code, label);

    @Test
    void should_hold_onto_values() {
        assertEquals(code, account.getCode());
        assertEquals(label, account.getLabel());
    }
}
