package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class CallbackTest {

    @Mock
    private CaseDetails<CaseData> caseDetails;

    private final Event event = Event.START_APPEAL;
    private final Optional<CaseDetails<CaseData>> caseDetailsBefore = Optional.empty();

    private Callback<CaseData> callback;

    @BeforeEach
    public void setUp() {

        callback = new Callback<>(caseDetails, caseDetailsBefore, event);
    }

    @Test
    public void should_hold_onto_values() {

        assertThat(caseDetails).isEqualToComparingOnlyGivenFields(callback.getCaseDetails());
        assertThat(caseDetailsBefore).isEqualTo(callback.getCaseDetailsBefore());
        assertThat(event).isEqualTo(callback.getEvent());
    }

}
