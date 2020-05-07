package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class CaseDetailsTest {

    @Mock
    private CaseData caseData;

    private final Long id = 123L;
    private String jurisdiction = "IA";
    private final State state = State.APPEAL_STARTED;

    private CaseDetails<CaseData> caseDetails = new CaseDetails<>(
        id,
        jurisdiction,
        state,
        caseData
    );

    @Test
    public void should_hold_onto_values() {

        assertThat(id).isEqualTo(caseDetails.getId());
        assertThat(jurisdiction).isEqualTo(caseDetails.getJurisdiction());
        assertThat(state).isEqualTo(caseDetails.getState());
    }

    @Test
    public void should_throw_required_field_missing_exception() {

        CaseDetails<CaseData> caseDetails = new CaseDetails<>(id, null, null, null);

        assertThatThrownBy(caseDetails::getJurisdiction)
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(caseDetails::getState)
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(caseDetails::getCaseData)
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
