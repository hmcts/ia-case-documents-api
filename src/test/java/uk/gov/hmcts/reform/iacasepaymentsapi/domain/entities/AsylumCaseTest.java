package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AsylumCaseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private AsylumCase asylumCase;
    private final String caseData = "{\"appealReferenceNumber\": \"PA/50222/2019\"}";

    @BeforeEach
    void setUp() throws IOException {
        asylumCase = objectMapper.readValue(caseData, AsylumCase.class);
    }

    @Test
    void read_appeal_reference_number() {
        Optional<String> maybeAppealReferenceNumberWithoutType = asylumCase.read(APPEAL_REFERENCE_NUMBER);
        Optional<String> maybeAppealReferenceNumberWithType = asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class);

        String appealReferenceNumberWithoutType = maybeAppealReferenceNumberWithoutType.orElseThrow(() ->
            new AssertionError("Expected value not found"));
        String appealReferenceNumberWithType = maybeAppealReferenceNumberWithType.orElseThrow(() ->
            new AssertionError("Expected value not found"));

        assertThat(appealReferenceNumberWithoutType).isEqualTo("PA/50222/2019")
            .isEqualTo(appealReferenceNumberWithType);
    }

    @Test
    void writes_simple_types() {
        asylumCase.write(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number");
        assertThat(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).get())
            .isEqualTo("some-appeal-reference-number");
    }

    @Test
    void clears_value() {
        asylumCase.clear(APPEAL_REFERENCE_NUMBER);
        assertThat(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).isEmpty();
    }
}
