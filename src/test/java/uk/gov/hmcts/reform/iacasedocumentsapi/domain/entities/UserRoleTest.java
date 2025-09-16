package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

public class UserRoleTest {

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    void to_string_gets_ids(UserRole userRole) {
        assertEquals(userRole.toString(), userRole.getId());
    }

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
    void get_tcw_correct_values() {
        assertTrue(UserRole.getCaseOfficerRoles().contains("caseworker-ia-caseofficer"));
        assertTrue(UserRole.getCaseOfficerRoles().contains("tribunal-caseworker"));
        assertTrue(UserRole.getCaseOfficerRoles().contains("challenged-access-legal-operations"));
        assertTrue(UserRole.getCaseOfficerRoles().contains("senior-tribunal-caseworker"));
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes_tcw_roles() {
        assertEquals(4, UserRole.getCaseOfficerRoles().size());
    }

    @Test
    void get_judge_roles_correct_values() {
        assertTrue(UserRole.getJudgeRoles().contains("caseworker-ia-iacjudge"));
        assertTrue(UserRole.getJudgeRoles().contains("caseworker-ia-judiciary"));
        assertTrue(UserRole.getJudgeRoles().contains("judge"));
        assertTrue(UserRole.getJudgeRoles().contains("senior-judge"));
        assertTrue(UserRole.getJudgeRoles().contains("leadership-judge"));
        assertTrue(UserRole.getJudgeRoles().contains("fee-paid-judge"));
        assertTrue(UserRole.getJudgeRoles().contains("lead-judge"));
        assertTrue(UserRole.getJudgeRoles().contains("hearing-judge"));
        assertTrue(UserRole.getJudgeRoles().contains("ftpa-judge"));
        assertTrue(UserRole.getJudgeRoles().contains("hearing-panel-judge"));
        assertTrue(UserRole.getJudgeRoles().contains("challenged-access-judiciary"));
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes_judge_roles() {
        assertEquals(11, UserRole.getJudgeRoles().size());
    }
}
