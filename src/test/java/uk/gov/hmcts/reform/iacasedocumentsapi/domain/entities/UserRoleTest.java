package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class UserRoleTest {

    @Test
    public void has_correct_values() {
        assertEquals("caseworker-ia-caseofficer", UserRole.CASE_OFFICER.toString());
        assertEquals("caseworker-ia-legalrep-solicitor", UserRole.LEGAL_REPRESENTATIVE.toString());
        assertEquals("caseworker-ia-judiciary", UserRole.JUDICIARY.toString());
        assertEquals("caseworker-ia-system", UserRole.SYSTEM.toString());
        assertEquals("caseworker-ia-respondentofficer", UserRole.HOME_OFFICE_GENERIC.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(5, UserRole.values().length);
    }
}
