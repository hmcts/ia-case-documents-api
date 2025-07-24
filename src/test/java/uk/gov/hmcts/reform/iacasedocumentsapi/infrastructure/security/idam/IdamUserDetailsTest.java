package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.idam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserRole;

public class IdamUserDetailsTest {

    private final String accessToken = "access-token";
    private final String id = "1234";
    private final List<String> roles = Arrays.asList("role-1", "role-2");
    private final String emailAddress = "email@example.com";
    private final String forename = "forename";
    private final String surname = "surname";

    private IdamUserDetails userDetails =
        new IdamUserDetails(
            accessToken,
            id,
            roles,
            emailAddress,
            forename,
            surname
        );

    @Test
    public void should_hold_onto_values() {

        assertEquals(accessToken, userDetails.getAccessToken());
        assertEquals(id, userDetails.getId());
        assertEquals(roles, userDetails.getRoles());
        assertEquals(emailAddress, userDetails.getEmailAddress());
        assertEquals(forename, userDetails.getForename());
        assertEquals(surname, userDetails.getSurname());
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {
        "CASE_OFFICER",
        "TRIBUNAL_CASEWORKER",
        "CHALLENGED_ACCESS_LEGAL_OPERATIONS",
        "SENIOR_TRIBUNAL_CASEWORKER"
    })
    void isLegalOfficer_should_be_true_for_tcw_roles(UserRole role) {
        userDetails =
            new IdamUserDetails(
                accessToken,
                id,
                Collections.singletonList(role.getId()),
                emailAddress,
                forename,
                surname
            );
        assertTrue(userDetails.isLegalOfficer());
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, mode = EnumSource.Mode.EXCLUDE, names = {
        "CASE_OFFICER",
        "TRIBUNAL_CASEWORKER",
        "CHALLENGED_ACCESS_LEGAL_OPERATIONS",
        "SENIOR_TRIBUNAL_CASEWORKER"
    })
    void isLegalOfficer_should_be_false_for_non_tcw_roles(UserRole role) {
        userDetails =
            new IdamUserDetails(
                accessToken,
                id,
                Collections.singletonList(role.getId()),
                emailAddress,
                forename,
                surname
            );
        assertFalse(userDetails.isLegalOfficer());
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {
        "IDAM_JUDGE",
        "JUDICIARY",
        "JUDGE",
        "SENIOR_JUDGE",
        "LEADERSHIP_JUDGE",
        "FEE_PAID_JUDGE",
        "LEAD_JUDGE",
        "HEARING_JUDGE",
        "FTPA_JUDGE",
        "HEARING_PANEL_JUDGE",
        "CHALLENGED_ACCESS_JUDICIARY"
    })
    void isJudge_should_be_true_for_judge_roles(UserRole role) {
        userDetails =
            new IdamUserDetails(
                accessToken,
                id,
                Collections.singletonList(role.getId()),
                emailAddress,
                forename,
                surname
            );
        assertTrue(userDetails.isJudge());
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, mode = EnumSource.Mode.EXCLUDE, names = {
        "IDAM_JUDGE",
        "JUDICIARY",
        "JUDGE",
        "SENIOR_JUDGE",
        "LEADERSHIP_JUDGE",
        "FEE_PAID_JUDGE",
        "LEAD_JUDGE",
        "HEARING_JUDGE",
        "FTPA_JUDGE",
        "HEARING_PANEL_JUDGE",
        "CHALLENGED_ACCESS_JUDICIARY"
    })
    void isJudge_should_be_false_for_non_judge_roles(UserRole role) {
        userDetails =
            new IdamUserDetails(
                accessToken,
                id,
                Collections.singletonList(role.getId()),
                emailAddress,
                forename,
                surname
            );
        assertFalse(userDetails.isJudge());
    }


}
