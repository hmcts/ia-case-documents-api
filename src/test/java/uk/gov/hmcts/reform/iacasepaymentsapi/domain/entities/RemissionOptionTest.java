package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RemissionOptionTest {

    @Test
    public void testJsonValueAnnotation() {
        assertEquals("asylumSupportFromHo", RemissionOption.ASYLUM_SUPPORT_FROM_HOME_OFFICE.toString());
        assertEquals("feeWaiverFromHo", RemissionOption.FEE_WAIVER_FROM_HOME_OFFICE.toString());
        assertEquals("under18GetSupportFromLocalAuthority", RemissionOption.UNDER_18_GET_SUPPORT.toString());
        assertEquals("parentGetSupportFromLocalAuthority", RemissionOption.PARENT_GET_SUPPORT.toString());
        assertEquals("noneOfTheseStatements", RemissionOption.NO_REMISSION.toString());
        assertEquals("iWantToGetHelpWithFees", RemissionOption.I_WANT_TO_GET_HELP_WITH_FEES.toString());
    }

    @Test
    public void testEnumValues() {
        RemissionOption[] options = RemissionOption.values();
        assertEquals(6, options.length);
        assertEquals(RemissionOption.ASYLUM_SUPPORT_FROM_HOME_OFFICE, options[0]);
        assertEquals(RemissionOption.FEE_WAIVER_FROM_HOME_OFFICE, options[1]);
        assertEquals(RemissionOption.UNDER_18_GET_SUPPORT, options[2]);
        assertEquals(RemissionOption.PARENT_GET_SUPPORT, options[3]);
        assertEquals(RemissionOption.NO_REMISSION, options[4]);
        assertEquals(RemissionOption.I_WANT_TO_GET_HELP_WITH_FEES, options[5]);
    }
}

