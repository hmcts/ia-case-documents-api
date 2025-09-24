package uk.gov.hmcts.reform.iacasedocumentsapi.utilities;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDateTime;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.util.Lists;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
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

        asylumCase.write(HEARING_DOCUMENTS,
            Lists.newArrayList(
                new IdValue<>("1", someDocumentWithMetadataWithDefaults(DocumentTag.CASE_SUMMARY)),
                new IdValue<>("2", someDocumentWithMetadataWithDefaults(DocumentTag.HEARING_NOTICE))
            )
        );

        return asylumCase;
    }

    public static DocumentWithMetadata someDocumentWithMetadataWithDefaults(DocumentTag documentTag) {
        return new DocumentWithMetadata(someDocumentWithDefaults(),
            "some-description",
            "",
            documentTag,"test");
    }

    public static Document someDocumentWithDefaults() {
        return new Document("some-doc-url",
            "some-doc-bin-url",
            "some-filename");
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

    public static String someAmUploadResponse() {
        return "{\n"
                +
                "    \"documents\": [\n"
                +
                "      {\n"
                +
                "        \"originalDocumentName\": \"some-appeal-reference-number-some-fname-decision-and-reasons-draft.docx\",\n"
                +
                "        \"links\": {\n"
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
                "}\n";
    }

    public static String someUserDetails() {
        return "{\n"
               + "  \"sub\": \"ia-caseofficer@fake.hmcts.net\",\n"
               + "  \"uid\": \"27ff8bb2-8bd3-4577-8ee1-33099acd50a5\",\n"
               + "  \"roles\": [\n"
               + "    \"caseworker-ia\",\n"
               + "    \"tribunal-caseworker\"\n"
               + "  ],\n"
               + "  \"name\": \"Officer\",\n"
               + "  \"given_name\": \"Case\",\n"
               + "  \"family_name\": \"Case Worker\"\n"
               + "}";
    }

    private AsylumCaseFixtures() {
        // for checkstyle
    }
}
