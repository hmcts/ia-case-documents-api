package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class UserRoleTest {
    @ParameterizedTest
    @CsvSource({
        "caseworker-ia-caseofficer, CASE_OFFICER",
        "tribunal-caseworker, TRIBUNAL_CASEWORKER",
        "challenged-access-legal-operations, CHALLENGED_ACCESS_LEGAL_OPERATIONS",
        "senior-tribunal-caseworker, SENIOR_TRIBUNAL_CASEWORKER",
        "caseworker-ia-admofficer, ADMIN_OFFICER",
        "hearing-centre-admin, HEARING_CENTRE_ADMIN",
        "ctsc, CTSC",
        "ctsc-team-leader, CTSC_TEAM_LEADER",
        "national-business-centre, NATIONAL_BUSINESS_CENTRE",
        "challenged-access-ctsc, CHALLENGED_ACCESS_CTSC",
        "challenged-access-admin, CHALLENGED_ACCESS_ADMIN",
        "caseworker-ia-iacjudge, IDAM_JUDGE",
        "caseworker-ia-judiciary, JUDICIARY",
        "judge, JUDGE",
        "senior-judge, SENIOR_JUDGE",
        "leadership-judge, LEADERSHIP_JUDGE",
        "fee-paid-judge, FEE_PAID_JUDGE",
        "lead-judge, LEAD_JUDGE",
        "hearing-judge, HEARING_JUDGE",
        "ftpa-judge, FTPA_JUDGE",
        "hearing-panel-judge, HEARING_PANEL_JUDGE",
        "challenged-access-judiciary, CHALLENGED_ACCESS_JUDICIARY",
        "caseworker-ia-legalrep-solicitor, LEGAL_REPRESENTATIVE",
        "caseworker-ia-system, SYSTEM",
        "caseworker-ia-homeofficeapc, HOME_OFFICE_APC",
        "caseworker-ia-homeofficelart, HOME_OFFICE_LART",
        "caseworker-ia-homeofficepou, HOME_OFFICE_POU",
        "caseworker-ia-respondentofficer, HOME_OFFICE_GENERIC",
        "citizen, CITIZEN",
        "unknown, UNKNOWN",
    })
    void has_correct_values(String expectedId, UserRole userRole) {
        assertEquals(expectedId, userRole.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(30, UserRole.values().length);
    }

    @Test
    void get_admin_officer_correct_values() {
        List<String> adminOfficerRoles = UserRole.getAdminOfficerRoles().stream().map(UserRole::toString).toList();
        assertTrue(adminOfficerRoles.contains("caseworker-ia-admofficer"));
        assertTrue(adminOfficerRoles.contains("hearing-centre-admin"));
        assertTrue(adminOfficerRoles.contains("ctsc"));
        assertTrue(adminOfficerRoles.contains("ctsc-team-leader"));
        assertTrue(adminOfficerRoles.contains("national-business-centre"));
        assertTrue(adminOfficerRoles.contains("challenged-access-ctsc"));
        assertTrue(adminOfficerRoles.contains("challenged-access-admin"));
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes_admin_officer_roles() {
        assertEquals(7, UserRole.getAdminOfficerRoles().size());
    }
}
