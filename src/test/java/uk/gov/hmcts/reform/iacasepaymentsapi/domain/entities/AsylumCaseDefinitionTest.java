package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AsylumCaseDefinitionTest {

    @Test
    public void has_correct_values() {

        assertEquals("appealReferenceNumber", AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER.value());
        assertEquals("oralFeeAmountForDisplay", AsylumCaseDefinition.ORAL_FEE_AMOUNT_FOR_DISPLAY.value());
        assertEquals("appealFeeDesc", AsylumCaseDefinition.APPEAL_FEE_DESC.value());
        assertEquals("paymentStatus", AsylumCaseDefinition.PAYMENT_STATUS.value());
        assertEquals("appealType", AsylumCaseDefinition.APPEAL_TYPE.value());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(16, AsylumCaseDefinition.values().length);
    }
}
