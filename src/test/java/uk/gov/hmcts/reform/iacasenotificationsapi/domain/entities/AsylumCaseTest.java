package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre.MANCHESTER;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

@SuppressWarnings("OperatorWrap")
public class AsylumCaseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void reads_string() throws IOException {

        String caseData = "{\"appealReferenceNumber\": \"PA/50222/2019\"}";
        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        Optional<String> maybeAppealReferenceNumber = asylumCase.read(APPEAL_REFERENCE_NUMBER);

        assertThat(maybeAppealReferenceNumber.get()).isEqualTo("PA/50222/2019");
    }

    @Test
    public void reads_hearing_centre() throws IOException {

        String caseData = "{\"hearingCentre\": \"manchester\"}";
        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        Optional<YesOrNo> maybeHearingCentre = asylumCase.read(HEARING_CENTRE);

        assertThat(maybeHearingCentre.get()).isEqualTo(MANCHESTER);
    }

    @Test
    public void reads_id_value_list() throws IOException {

        String caseData = "{\"directions\": [\n" +
                          "    {\n" +
                          "      \"id\": \"2\",\n" +
                          "      \"value\": {\n" +
                          "        \"tag\": \"buildCase\",\n" +
                          "        \"dateDue\": \"2019-06-13\",\n" +
                          "        \"parties\": \"legalRepresentative\",\n" +
                          "        \"dateSent\": \"2019-05-16\",\n" +
                          "        \"explanation\": \"some-explanation\"\n" +
                          "      }\n" +
                          "    },\n" +
                          "    {\n" +
                          "      \"id\": \"1\",\n" +
                          "      \"value\": {\n" +
                          "        \"tag\": \"respondentEvidence\",\n" +
                          "        \"dateDue\": \"2019-05-30\",\n" +
                          "        \"parties\": \"respondent\",\n" +
                          "        \"dateSent\": \"2019-05-16\",\n" +
                          "        \"explanation\": \"some-other-explanation\"\n" +
                          "      }\n" +
                          "    }\n" +
                          "  ]}";

        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        Optional<List<IdValue<Direction>>> maybeRespondentDocuments = asylumCase.read(DIRECTIONS);

        List<IdValue<Direction>> idValues = maybeRespondentDocuments.get();

        Direction direction1 = idValues.get(0).getValue();
        Direction direction2 = idValues.get(1).getValue();

        assertThat(idValues.get(0).getId()).isEqualTo("2");
        assertThat(direction1.getTag()).isEqualTo(DirectionTag.BUILD_CASE);
        assertThat(direction1.getDateDue()).isEqualTo("2019-06-13");
        assertThat(direction1.getParties()).isEqualTo(Parties.LEGAL_REPRESENTATIVE);
        assertThat(direction1.getDateSent()).isEqualTo("2019-05-16");
        assertThat(direction1.getExplanation()).isEqualTo("some-explanation");

        assertThat(idValues.get(1).getId()).isEqualTo("1");
        assertThat(direction2.getTag()).isEqualTo(DirectionTag.RESPONDENT_EVIDENCE);
        assertThat(direction2.getDateDue()).isEqualTo("2019-05-30");
        assertThat(direction2.getParties()).isEqualTo(Parties.RESPONDENT);
        assertThat(direction2.getDateSent()).isEqualTo("2019-05-16");
        assertThat(direction2.getExplanation()).isEqualTo("some-other-explanation");
    }

    @Test
    public void reads_using_parameter_type_generics() throws IOException {

        String caseData = "{\"appealReferenceNumber\": \"PA/50222/2019\"}";
        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        assertThat(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).get())
            .isEqualTo("PA/50222/2019");
    }

    @Test
    public void writes_simple_type() {

        AsylumCase asylumCase = new AsylumCase();

        asylumCase.write(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number");

        assertThat(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).get())
            .isEqualTo("some-appeal-reference-number");
    }

    @Test
    public void writes_complex_type() {

        AsylumCase asylumCase = new AsylumCase();

        IdValue<Direction> idValue = new IdValue<>(
            "some-id",
            new Direction(
                "some-explanation",
                Parties.BOTH,
                "some-date",
                "some-other-date",
                DirectionTag.CASE_EDIT,
                Collections.emptyList()));


        asylumCase.write(DIRECTIONS, asList(idValue));


        Optional<List<IdValue<Direction>>> maybeDocuments = asylumCase.read(DIRECTIONS);

        IdValue<Direction> documents = maybeDocuments.get().get(0);


        assertThat(maybeDocuments.get().size()).isEqualTo(1);

        assertThat(documents.getId()).isEqualTo("some-id");

        assertThat(documents.getValue().getTag()).isEqualTo(DirectionTag.CASE_EDIT);

        assertThat(documents.getValue().getDateDue()).isEqualTo("some-date");

        assertThat(documents.getValue().getDateSent()).isEqualTo("some-other-date");

        assertThat(documents.getValue().getExplanation()).isEqualTo("some-explanation");
    }
}
