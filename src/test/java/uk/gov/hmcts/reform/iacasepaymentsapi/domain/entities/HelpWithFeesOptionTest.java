package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HelpWithFeesOptionTest {

    @Test
    public void testJsonValueAnnotation() {
        assertEquals("wantToApply", HelpWithFeesOption.WANT_TO_APPLY.toString());
        assertEquals("alreadyApplied", HelpWithFeesOption.ALREADY_APPLIED.toString());
        assertEquals("willPayForAppeal", HelpWithFeesOption.WILL_PAY_FOR_APPEAL.toString());
    }

    @Test
    public void testEnumValues() {
        HelpWithFeesOption[] options = HelpWithFeesOption.values();
        assertEquals(3, options.length);
        assertEquals(HelpWithFeesOption.WANT_TO_APPLY, options[0]);
        assertEquals(HelpWithFeesOption.ALREADY_APPLIED, options[1]);
        assertEquals(HelpWithFeesOption.WILL_PAY_FOR_APPEAL, options[2]);
    }
}
