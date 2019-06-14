package uk.gov.hmcts.reform.iacasedocumentsapi.utilities;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SCHEDULE_OF_ISSUES_DISAGREEMENT_DESCRIPTION;

import java.time.LocalDateTime;
import org.apache.commons.lang.RandomStringUtils;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;

public class AsylumCaseFixtures {

    public static AsylumCase someAsylumCaseWithDefaults() {

        AsylumCase asylumCase = new AsylumCase();

        asylumCase.write(APPEAL_REFERENCE_NUMBER, "RP/500001/2019");
        asylumCase.write(APPELLANT_FAMILY_NAME, "some-fname");

        asylumCase.write(LIST_CASE_HEARING_CENTRE, HearingCentre.MANCHESTER.getValue());
        asylumCase.write(LIST_CASE_HEARING_DATE, someDate());
        asylumCase.write(APPELLANT_GIVEN_NAMES, someString());
        asylumCase.write(APPELLANT_FAMILY_NAME, someString());
        asylumCase.write(ANONYMITY_ORDER, YesOrNo.YES);

        asylumCase.write(APPELLANT_REPRESENTATIVE, someString());
        asylumCase.write(RESPONDENT_REPRESENTATIVE, someString());

        asylumCase.write(CASE_INTRODUCTION_DESCRIPTION, someString());
        asylumCase.write(APPELLANT_CASE_SUMMARY_DESCRIPTION, someString());

        asylumCase.write(IMMIGRATION_HISTORY_AGREEMENT, YesOrNo.YES);
        asylumCase.write(AGREED_IMMIGRATION_HISTORY_DESCRIPTION, someString());
        asylumCase.write(RESPONDENTS_IMMIGRATION_HISTORY_DESCRIPTION, someString());
        asylumCase.write(IMMIGRATION_HISTORY_DISAGREEMENT_DESCRIPTION, someString());

        asylumCase.write(SCHEDULE_OF_ISSUES_AGREEMENT, YesOrNo.YES);
        asylumCase.write(APPELLANTS_AGREED_SCHEDULE_OF_ISSUES_DESCRIPTION, someString());
        asylumCase.write(APPELLANTS_DISPUTED_SCHEDULE_OF_ISSUES_DESCRIPTION, someString());
        asylumCase.write(SCHEDULE_OF_ISSUES_DISAGREEMENT_DESCRIPTION, someString());

        return asylumCase;
    }

    private static String someDate() {
        return LocalDateTime.now().toString();
    }

    private static String someString() {
        return RandomStringUtils.randomAlphabetic(8);
    }

    public static String someUploadResponse() {
        return "{\n"
                +
                "  \"_embedded\": {\n"
                +
                "    \"documents\": [\n"
                +
                "      {\n"
                +
                "        \"originalDocumentName\": \"some-document-name\",\n"
                +
                "        \"_links\": {\n"
                +
                "          \"self\": {\n"
                +
                "            \"href\": \"\"\n"
                +
                "          },\n"
                +
                "          \"binary\": {\n"
                +
                "            \"href\": \"\"\n"
                +
                "          }\n"
                +
                "      }\n"
                +
                "    }\n"
                +
                "    ]\n"
                +
                "  }\n"
                +
                "}\n";
    }

    public static String someUserDetails() {
        return "{\n"
                +
                "  \"id\": \"1\",\n"
                +
                "  \"roles\": [\"caseworker-ia-caseofficer\"],\n"
                +
                "  \"email\": \"someone@somewhere.com\",\n"
                +
                "  \"forename\": \"some-fname\",\n"
                +
                "  \"surname\": \"some-sname\"\n"
                +
                "}";
    }

    private AsylumCaseFixtures() {
        // for checkstyle
    }
}
