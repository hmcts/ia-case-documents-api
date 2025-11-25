package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

import java.util.Optional;
import java.util.Set;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HomeOfficeEmailFinderTest {
    String apcHomeOfficeEmailAddress = "apcHomeOffice@Email.com";
    String lartHomeOfficeEmailAddress = "lartHomeOffice@Email.com";
    String listCaseFtpaHomeOfficeEmailAddress = "listCaseFtpa@Email.com";
    String homeOfficeEmailAddress = "homeOffice@Email.com";
    String listCaseHomeOfficeEmailAddress = "listCaseHo@Email.com";

    @Mock
    AppealService appealService;
    @Mock EmailAddressFinder emailAddressFinder;
    @Mock AsylumCase asylumCase;

    HomeOfficeEmailFinder homeOfficeEmailFinder;


    @BeforeEach
    void setUp() {
        homeOfficeEmailFinder = new HomeOfficeEmailFinder(appealService,
                emailAddressFinder,
                apcHomeOfficeEmailAddress,
                lartHomeOfficeEmailAddress);
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "APPEAL_SUBMITTED",
        "PENDING_PAYMENT",
        "AWAITING_RESPONDENT_EVIDENCE",
        "AWAITING_CLARIFYING_QUESTIONS_ANSWERS",
        "CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED"
    })
    public void should_return_apcHomeOfficeEmailAddress_based_on_state(State state) {
        // given
        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
                .thenReturn(Optional.of(state));
        // when
        Set<String> recipientsList = homeOfficeEmailFinder.getRecipientsList(asylumCase);
        // then
        assertTrue(recipientsList.contains(apcHomeOfficeEmailAddress));
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "CASE_BUILDING",
        "CASE_UNDER_REVIEW",
        "RESPONDENT_REVIEW",
        "AWAITING_REASONS_FOR_APPEAL",
        "REASONS_FOR_APPEAL_SUBMITTED"
    })
    public void should_return_lartHomeOfficeEmailAddress_based_on_state(State state) {
        // given
        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
                .thenReturn(Optional.of(state));
        // when
        Set<String> recipientsList = homeOfficeEmailFinder.getRecipientsList(asylumCase);
        // then
        assertTrue(recipientsList.contains(lartHomeOfficeEmailAddress));
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "FTPA_SUBMITTED",
        "FTPA_DECIDED"
    })
    public void should_return_list_case_ftpa_ho_email_address(State state) {
        when(emailAddressFinder.getListCaseFtpaHomeOfficeEmailAddress(asylumCase)).thenReturn(listCaseFtpaHomeOfficeEmailAddress);
        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
                .thenReturn(Optional.of(state));
        Set<String> recipientsList = homeOfficeEmailFinder.getRecipientsList(asylumCase);
        assertTrue(recipientsList.contains(listCaseFtpaHomeOfficeEmailAddress));
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "LISTING",
        "SUBMIT_HEARING_REQUIREMENTS",
        "ENDED",
        "APPEAL_TAKEN_OFFLINE"
    })
    public void should_return_list_case_ho_email_address_appeal_not_listed(State state) {
        when(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase)).thenReturn(homeOfficeEmailAddress);
        when(appealService.isAppealListed(asylumCase)).thenReturn(false);
        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
                .thenReturn(Optional.of(state));
        Set<String> recipientsList = homeOfficeEmailFinder.getRecipientsList(asylumCase);
        assertTrue(recipientsList.contains(homeOfficeEmailAddress));
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "PREPARE_FOR_HEARING",
        "FINAL_BUNDLING",
        "PRE_HEARING",
        "DECISION",
        "ADJOURNED",
        "DECIDED",
        "ENDED",
        "APPEAL_TAKEN_OFFLINE"
    })
    public void should_return_home_office_email_address_appeal_listed(State state) {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        when(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase)).thenReturn(listCaseHomeOfficeEmailAddress);
        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
                .thenReturn(Optional.of(state));
        Set<String> recipientsList = homeOfficeEmailFinder.getRecipientsList(asylumCase);
        assertTrue(recipientsList.contains(listCaseHomeOfficeEmailAddress));
    }

    @Test
    public void should_throw_exception_when_state_is_not_present() {
        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> homeOfficeEmailFinder.getRecipientsList(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("currentCaseStateVisibleToHomeOfficeAll flag is not present");
    }
}
