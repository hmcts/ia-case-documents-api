package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AsylumCaseDefinitionTest {

    @Test
    public void has_correct_values() {

        assertEquals("appealReferenceNumber", AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER.value());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(1, AsylumCaseDefinition.values().length);
    }
}
