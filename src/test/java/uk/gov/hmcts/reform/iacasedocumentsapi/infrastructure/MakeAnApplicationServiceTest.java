package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DECIDE_AN_APPLICATION_ID;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.MAKE_AN_APPLICATIONS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.MakeAnApplicationService.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplicationTypes;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MakeAnApplicationServiceTest {
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private List<IdValue<MakeAnApplication>> applications;
    @Mock
    private MakeAnApplication makeAnApplication;

    private MakeAnApplicationService makeAnApplicationService;
    private String decideAnApplicationId = "2";

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
    void should_return_application_when_not_decided() {
        Optional<MakeAnApplication> makeAnApplicationOptional = makeAnApplicationService.getMakeAnApplication(asylumCase, false);
        assertEquals("Withdraw", makeAnApplicationOptional.get().getType());
    }

    @Test
    void should_return_application_when_decided() {
        when(asylumCase.read(DECIDE_AN_APPLICATION_ID)).thenReturn(Optional.of(decideAnApplicationId));
        Optional<MakeAnApplication> makeAnApplicationOptional = makeAnApplicationService.getMakeAnApplication(asylumCase, true);
        assertEquals("Other", makeAnApplicationOptional.get().getType());
    }

    @Test
    void isAppealListed() {
        State state = State.APPEAL_SUBMITTED;
        assertFalse(makeAnApplicationService.isApplicationListed(state));

        state = State.ADJOURNED;
        assertTrue(makeAnApplicationService.isApplicationListed(state));
    }

    @ParameterizedTest
    @EnumSource(value = MakeAnApplicationTypes.class)
    void shouldMapApplicationTypeToPhrase(MakeAnApplicationTypes makeAnApplicationTypes) {
        when(makeAnApplication.getType()).thenReturn(makeAnApplicationTypes.toString());

        String expectedPhrase = makeAnApplicationService.mapApplicationTypeToPhrase(makeAnApplication);

        switch (makeAnApplicationTypes) {
            case ADJOURN:
                assertEquals(expectedPhrase, "change the hearing date");
                break;
            case EXPEDITE:
                assertEquals(expectedPhrase, "have the hearing sooner");
                break;
            case JUDGE_REVIEW:
                assertEquals(expectedPhrase, "ask a judge to review the decision");
                break;
            case LINK_OR_UNLINK:
                assertEquals(expectedPhrase, "link or unlink the appeal");
                break;
            case TIME_EXTENSION:
                assertEquals(expectedPhrase, "ask for more time");
                break;
            case TRANSFER:
                assertEquals(expectedPhrase, "move the hearing to a different location");
                break;
            case WITHDRAW:
                assertEquals(expectedPhrase, "withdraw from the appeal");
                break;
            case UPDATE_HEARING_REQUIREMENTS:
                assertEquals(expectedPhrase, "change some of the hearing requirements");
                break;
            case UPDATE_APPEAL_DETAILS:
                assertEquals(expectedPhrase, "change some of the appeal details");
                break;
            case REINSTATE:
                assertEquals(expectedPhrase, "reinstate the appeal");
                break;
            case OTHER:
                assertEquals(expectedPhrase, "change something about the appeal");
                break;
            default:
                break;
        }
    }

    @Test
    void should_throw_exception_if_application_type_cannot_be_parsed() {
        assertThatThrownBy(() -> makeAnApplicationService.getApplicationTypes("test"))
                .hasMessage("Application type could not be parsed")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_retrieve_correct_application_properties() {
        final MakeAnApplication testApplication = new MakeAnApplication(
                "Admin Officer",
                "someRandomApplicationTypeThatShouldCauseAnException",
                "someRandomDetails",
                new ArrayList<>(),
                LocalDate.now().toString(),
                "Granted",
                State.APPEAL_SUBMITTED.toString(),
                "caseworker-ia-admofficer");
        testApplication.setDecisionReason("No reason");

        Map<String, String> applicationPropertiesMap = makeAnApplicationService.retrieveApplicationProperties(Optional.of(testApplication));
        assertEquals(applicationPropertiesMap.get(APPLICATION_TYPE), testApplication.getType());
        assertEquals(applicationPropertiesMap.get(APPLICATION_DECISION), testApplication.getDecision());
        assertEquals(applicationPropertiesMap.get(APPLICATION_DECISION_REASON), testApplication.getDecisionReason());
    }
}

