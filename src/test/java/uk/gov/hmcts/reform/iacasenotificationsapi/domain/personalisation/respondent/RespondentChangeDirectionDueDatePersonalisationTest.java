package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType.AIP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Parties.RESPONDENT;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
public class RespondentChangeDirectionDueDatePersonalisationTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    AppealService appealService;

    private final Long caseId = 12345L;
    private final String afterListingTemplateId = "afterListingTemplateId";
    private final String beforeListingTemplateId = "beforeListingTemplateId";
    private final String appellantTemplateId = "appellantTemplateId";
    private final String iaExUiFrontendUrl = "http://localhost";
    private final String apcHomeOfficeEmailAddress = "homeoffice-apc@example.com";
    private final String lartHomeOfficeEmailAddress = "homeoffice-respondent@example.com";
    private final String homeOfficeHearingCentreEmail = "hc-taylorhouse@example.com";
    private final String homeOfficeEmail = "ho-taylorhouse@example.com";
    private final String homeOfficeFtpaEmailAddress = "ho-ftpa-taylorhouse@example.com";
    private final String hmctsReference = "hmctsReference";
    private final String ariaListingReference = "someAriaListingReference";
    private final String homeOfficeReference = "homeOfficeReference";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "cust.services@example.com";


    private RespondentChangeDirectionDueDatePersonalisation respondentChangeDirectionDueDatePersonalisation;

    @BeforeEach
    public void setUp() {

        when((emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase)))
                .thenReturn(homeOfficeHearingCentreEmail);

        when(emailAddressFinder.getListCaseFtpaHomeOfficeEmailAddress(asylumCase)).thenReturn(homeOfficeFtpaEmailAddress);

        when((emailAddressFinder.getHomeOfficeEmailAddress(asylumCase))).thenReturn(homeOfficeEmail);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));

        respondentChangeDirectionDueDatePersonalisation = new RespondentChangeDirectionDueDatePersonalisation(
            afterListingTemplateId,
            beforeListingTemplateId,
            appellantTemplateId,
            iaExUiFrontendUrl,
            personalisationProvider,
            apcHomeOfficeEmailAddress,
            lartHomeOfficeEmailAddress,
            customerServicesProvider,
            appealService,
            emailAddressFinder
        );
    }

    @Test
    void should_return_the_given_before_listing_template_id() {
        when(asylumCase.read(AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
                .thenReturn(Optional.of(State.CASE_BUILDING));

        assertEquals(beforeListingTemplateId,
                respondentChangeDirectionDueDatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_the_given_after_listing_template_id() {
        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        assertEquals(afterListingTemplateId, respondentChangeDirectionDueDatePersonalisation.getTemplateId(asylumCase));
    }

    @ParameterizedTest
    @EnumSource(value = Parties.class, names = {
        "RESPONDENT",
        "LEGAL_REPRESENTATIVE",
        "BOTH",
        "APPELLANT",
        "APPELLANT_AND_RESPONDENT"
    })
    void should_return_the_given_aip_template_id_when_direction_it_to_respondent(Parties parties) {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(AIP));
        when(asylumCase.read(DIRECTION_EDIT_PARTIES, Parties.class)).thenReturn(Optional.of(parties));

        String templateId = respondentChangeDirectionDueDatePersonalisation.getTemplateId(asylumCase);

        if (!parties.equals(RESPONDENT)) {
            assertEquals(appellantTemplateId, templateId);
        } else {
            assertNotEquals(appellantTemplateId, templateId);
        }

    }

    @Test
    void should_return_correct_email_address_for_home_office() {

        List<State> apcEmail = newArrayList(
                State.APPEAL_SUBMITTED,
                State.PENDING_PAYMENT,
                State.AWAITING_RESPONDENT_EVIDENCE,
                State.AWAITING_REASONS_FOR_APPEAL,
                State.REASONS_FOR_APPEAL_SUBMITTED,
                State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS,
                State.CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED,
                State.AWAITING_CMA_REQUIREMENTS,
                State.CMA_REQUIREMENTS_SUBMITTED,
                State.CMA_ADJUSTMENTS_AGREED
        );


        List<State> lartEmail = newArrayList(
                State.CASE_BUILDING,
                State.CASE_UNDER_REVIEW,
                State.RESPONDENT_REVIEW
        );

        List<State> ftpaEmail = newArrayList(
                State.FTPA_SUBMITTED,
                State.FTPA_DECIDED
        );

        List<State> pouNoListedEmail = newArrayList(
                State.LISTING,
                State.SUBMIT_HEARING_REQUIREMENTS,
                State.ENDED,
                State.APPEAL_TAKEN_OFFLINE
        );

        List<State> poulistedEmail = newArrayList(
                State.PREPARE_FOR_HEARING,
                State.FINAL_BUNDLING,
                State.PRE_HEARING,
                State.DECISION,
                State.ADJOURNED,
                State.DECIDED,
                State.ENDED,
                State.APPEAL_TAKEN_OFFLINE,
                State.CMA_LISTED
        );

        Map<String, List<State>> states = new HashMap<>();

        states.put(apcHomeOfficeEmailAddress, apcEmail);
        states.put(lartHomeOfficeEmailAddress, lartEmail);
        states.put(homeOfficeFtpaEmailAddress, ftpaEmail);
        states.put(homeOfficeEmail, pouNoListedEmail);
        states.put(homeOfficeHearingCentreEmail, poulistedEmail);


        Set<String> emailAddresses = states.keySet();

        for (String emailAddress : emailAddresses) {
            List<State> statesList = states.get(emailAddress);
            for (State state : statesList) {
                when(asylumCase.read(AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
                        .thenReturn(Optional.of(state));

                if (emailAddress != null && emailAddress.equals(homeOfficeHearingCentreEmail)) {
                    // test the same state when the case is listed
                    when(appealService.isAppealListed(asylumCase)).thenReturn(true);
                    when(asylumCase.read(HEARING_CENTRE)).thenReturn(Optional.of(Optional.empty()));
                    when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                            .thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

                    assertTrue(respondentChangeDirectionDueDatePersonalisation.getRecipientsList(asylumCase)
                            .contains(homeOfficeHearingCentreEmail));
                } else if (emailAddress != null && emailAddress.equals(homeOfficeEmail)) {
                    //case not listed yet
                    when(appealService.isAppealListed(asylumCase)).thenReturn(false);
                    when(asylumCase.read(HEARING_CENTRE)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
                    when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

                    assertTrue(respondentChangeDirectionDueDatePersonalisation.getRecipientsList(asylumCase)
                            .contains(homeOfficeEmail));
                } else {
                    assertTrue(respondentChangeDirectionDueDatePersonalisation.getRecipientsList(asylumCase)
                            .contains(emailAddress));
                }
            }
        }
    }

    @Test
    void should_throw_exception_when_home_office_is_missing_in_the_case_data() {
        when(asylumCase.read(AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> respondentChangeDirectionDueDatePersonalisation.getRecipientsList(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("currentCaseStateVisibleToHomeOfficeAll flag is not present");
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_RESPONDENT_CHANGE_DIRECTION_DUE_DATE",
                respondentChangeDirectionDueDatePersonalisation.getReferenceId(caseId));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    void should_return_given_personalisation_when_all_information_given(YesOrNo isAda) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(respondentChangeDirectionDueDatePersonalisation);
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisation());

        Map<String, String> personalisation =
                respondentChangeDirectionDueDatePersonalisation.getPersonalisation(callback);

        assertThat(personalisation).isNotEmpty();
        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(
                () -> respondentChangeDirectionDueDatePersonalisation.getPersonalisation((Callback<AsylumCase>) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("callback must not be null");
    }

    private Map<String, String> getPersonalisation() {
        return ImmutableMap
                .<String, String>builder()
                .put("hmctsReference", hmctsReference)
                .put("ariaListingReference", ariaListingReference)
                .put("homeOfficeReference", homeOfficeReference)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantFamilyName)
                .put("customerServicesTelephone", customerServicesTelephone)
                .put("customerServicesEmail", customerServicesEmail)
                .build();
    }
}
