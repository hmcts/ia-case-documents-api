package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AsylumCaseTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void read_string() throws IOException {

        String caseData = "{\"appealReferenceNumber\": \"PA/50222/2019\"}";
        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        Optional<String> maybeAppealReferenceNumber = asylumCase.read(APPEAL_REFERENCE_NUMBER);

        assertThat(maybeAppealReferenceNumber.get()).isEqualTo("PA/50222/2019");
    }

    @Test
    void read_using_parameters_type_generics() throws IOException {

        String caseData = "{\"appealReferenceNumber\": \"PA/50222/2019\"}";
        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        Optional<String> maybeAppealReferenceNumber = asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class);

        assertThat(maybeAppealReferenceNumber.get()).isEqualTo("PA/50222/2019");
    }

    @Test
    void writes_simple_types() {

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number");

        assertThat(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).get())
            .isEqualTo("some-appeal-reference-number");
    }

    @Test
    void clears_value() throws IOException {

        String caseData = "{\"appealReferenceNumber\": \"PA/50222/2019\"}";
        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        asylumCase.clear(APPEAL_REFERENCE_NUMBER);

        assertThat(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).isEmpty();
    }
}
