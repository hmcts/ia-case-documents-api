package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DECIDE_AN_APPLICATION_ID;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.MAKE_AN_APPLICATIONS;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MakeAnApplicationServiceTest {
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private List<IdValue<MakeAnApplication>> applications;

    private MakeAnApplicationService makeAnApplicationService;
    private String decideAnApplicationId = "3";

    @BeforeEach
    public void setup() {
        List<IdValue<MakeAnApplication>> makeAnApplications = new ArrayList<>();
        MakeAnApplication makeAnApplication1 = new MakeAnApplication(
                "",
                "Expedite",
                "",
                new ArrayList<>(),
                "",
                "",
                "",
                "");
        makeAnApplications.add(new IdValue<>("1", makeAnApplication1));
        MakeAnApplication makeAnApplication2 = new MakeAnApplication(
                "",
                "Other",
                "",
                new ArrayList<>(),
                "",
                "",
                "",
                "");
        makeAnApplications.add(new IdValue<>("2", makeAnApplication2));
        MakeAnApplication makeAnApplication3 = new MakeAnApplication(
                "",
                "Withdraw",
                "",
                new ArrayList<>(),
                "",
                "",
                "",
                "");
        makeAnApplications.add(new IdValue<>("3", makeAnApplication3));
        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(makeAnApplications));
        makeAnApplicationService = new MakeAnApplicationService();
    }

    @Test
    public void should_return_application_when_not_decided() {
        Optional<MakeAnApplication> makeAnApplicationOptional = makeAnApplicationService.getMakeAnApplication(asylumCase, false);
        assertEquals("Expedite", makeAnApplicationOptional.get().getType());
    }

    @Test
    public void should_return_application_when_decided() {
        when(asylumCase.read(DECIDE_AN_APPLICATION_ID)).thenReturn(Optional.of(decideAnApplicationId));
        Optional<MakeAnApplication> makeAnApplicationOptional = makeAnApplicationService.getMakeAnApplication(asylumCase, true);
        assertEquals("Withdraw", makeAnApplicationOptional.get().getType());
    }

    @Test
    public void isAppealListed() {
        State state = State.APPEAL_SUBMITTED;
        assertFalse(makeAnApplicationService.isApplicationListed(state));

        state = State.ADJOURNED;
        assertTrue(makeAnApplicationService.isApplicationListed(state));
    }
}
