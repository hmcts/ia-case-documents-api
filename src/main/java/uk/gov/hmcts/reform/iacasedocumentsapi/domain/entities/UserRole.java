package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.List;
import lombok.Getter;

@Getter
public enum UserRole {

    // caseworker-ia-caseofficer
    CASE_OFFICER("caseworker-ia-caseofficer"),
    TRIBUNAL_CASEWORKER("tribunal-caseworker"),
    CHALLENGED_ACCESS_LEGAL_OPERATIONS("challenged-access-legal-operations"),
    SENIOR_TRIBUNAL_CASEWORKER("senior-tribunal-caseworker"),
    // caseworker-ia-admofficer
    ADMIN_OFFICER("caseworker-ia-admofficer"),
    HEARING_CENTRE_ADMIN("hearing-centre-admin"),
    CTSC("ctsc"),
    CTSC_TEAM_LEADER("ctsc-team-leader"),
    NATIONAL_BUSINESS_CENTRE("national-business-centre"),
    CHALLENGED_ACCESS_CTSC("challenged-access-ctsc"),
    CHALLENGED_ACCESS_ADMIN("challenged-access-admin"),
    // caseworker-ia-iacjudge
    IDAM_JUDGE("caseworker-ia-iacjudge"),
    JUDICIARY("caseworker-ia-judiciary"),
    JUDGE("judge"),
    SENIOR_JUDGE("senior-judge"),
    LEADERSHIP_JUDGE("leadership-judge"),
    FEE_PAID_JUDGE("fee-paid-judge"),
    LEAD_JUDGE("lead-judge"),
    HEARING_JUDGE("hearing-judge"),
    FTPA_JUDGE("ftpa-judge"),
    HEARING_PANEL_JUDGE("hearing-panel-judge"),
    CHALLENGED_ACCESS_JUDICIARY("challenged-access-judiciary"),

    LEGAL_REPRESENTATIVE("caseworker-ia-legalrep-solicitor"),
    SYSTEM("caseworker-ia-system"),
    HOME_OFFICE_APC("caseworker-ia-homeofficeapc"),
    HOME_OFFICE_LART("caseworker-ia-homeofficelart"),
    HOME_OFFICE_POU("caseworker-ia-homeofficepou"),
    HOME_OFFICE_GENERIC("caseworker-ia-respondentofficer"),
    CITIZEN("citizen");

    @JsonValue
    private final String id;

    UserRole(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }


    public static List<String> getCaseOfficerRoles() {
        return List.of(
            CASE_OFFICER.getId(),
            TRIBUNAL_CASEWORKER.getId(),
            CHALLENGED_ACCESS_LEGAL_OPERATIONS.getId(),
            SENIOR_TRIBUNAL_CASEWORKER.getId()
        );
    }

    public static List<String> getJudgeRoles() {
        return List.of(
            IDAM_JUDGE.getId(),
            JUDICIARY.getId(),
            JUDGE.getId(),
            SENIOR_JUDGE.getId(),
            LEADERSHIP_JUDGE.getId(),
            FEE_PAID_JUDGE.getId(),
            LEAD_JUDGE.getId(),
            HEARING_JUDGE.getId(),
            FTPA_JUDGE.getId(),
            HEARING_PANEL_JUDGE.getId(),
            CHALLENGED_ACCESS_JUDICIARY.getId()
        );
    }
}
