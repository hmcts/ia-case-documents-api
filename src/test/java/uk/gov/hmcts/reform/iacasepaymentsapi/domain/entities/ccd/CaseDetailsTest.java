package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class CaseDetailsTest {

    @Mock
    private CaseData caseData;

    private final Long id = 123L;
    private final String jurisdiction = "IA";
    private final State state = State.APPEAL_STARTED;

    private CaseDetails<CaseData> caseDetails;

    @BeforeEach
    void setUp() {
        caseDetails = new CaseDetails<>(id, jurisdiction, state, caseData);
    }

    @Test
    void holds_onto_values() {
        assertThat(id).isEqualTo(caseDetails.getId());
        assertThat(jurisdiction).isEqualTo(caseDetails.getJurisdiction());
        assertThat(state).isEqualTo(caseDetails.getState());
    }

    @Test
    void throws_required_field_missing_exception() {
        CaseDetails<CaseData> caseDetails = new CaseDetails<>(id, null, null, null);

        assertThrows(NullPointerException.class, caseDetails::getJurisdiction);
        assertThrows(NullPointerException.class, caseDetails::getState);
        assertThrows(NullPointerException.class, caseDetails::getCaseData);
    }
}
