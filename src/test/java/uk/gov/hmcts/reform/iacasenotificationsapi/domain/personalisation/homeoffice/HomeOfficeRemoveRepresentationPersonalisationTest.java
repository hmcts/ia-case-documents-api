package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HomeOfficeRemoveRepresentationPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    AppealService appealService;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    EmailAddressFinder emailAddressFinder;

    private final Long caseId = 12345L;
    private final String templateIdBeforeListing = "beforeTemplateId";
    private final String templateIdAfterListing = "afterTemplateId";
    private final String iaExUiFrontendUrl = "http://somefrontendurl";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String ariaListingReference = "someReferenceNumber";
    private final String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private String apcPrivateBetaInboxHomeOfficeEmailAddress = "homeoffice-apc@example.com";
    private String respondentReviewDirectionEmail = "homeoffice-respondent@example.com";
    private String homeOfficeHearingCentreEmail = "hc-taylorhouse@example.com";
    private String homeOfficeEmail = "ho-taylorhouse@example.com";

    private final String addressLine1 = "A";
    private final String addressLine2 = "B";
    private final String addressLine3 = "C";
    private final String postTown = "D";
    private final String county = "E";
    private final String postCode = "F";
    private final String country = "G";

    private AddressUk addressUk = new AddressUk(
        addressLine1,
        addressLine2,
        addressLine3,
        postTown,
        county,
        postCode,
        country
    );

    private HomeOfficeRemoveRepresentationPersonalisation
        homeOfficeRemoveRepresentationPersonalisation;

    @BeforeEach
    public void setUp() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when((emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase))).thenReturn(homeOfficeHearingCentreEmail);
        when((emailAddressFinder.getHomeOfficeEmailAddress(asylumCase))).thenReturn(homeOfficeEmail);

        homeOfficeRemoveRepresentationPersonalisation =
            new HomeOfficeRemoveRepresentationPersonalisation(
                templateIdBeforeListing,
                templateIdAfterListing,
                apcPrivateBetaInboxHomeOfficeEmailAddress,
                respondentReviewDirectionEmail,
                iaExUiFrontendUrl,
                appealService,
                emailAddressFinder,
                customerServicesProvider
            );
    }

    @Test
    void should_return_apc_email_address_for_given_states() {

        List<State> apcStates = newArrayList(
            State.APPEAL_STARTED,
            State.APPEAL_SUBMITTED,
            State.AWAITING_RESPONDENT_EVIDENCE,
            State.CASE_BUILDING,
            State.CASE_UNDER_REVIEW,
            State.PENDING_PAYMENT,
            State.ENDED
        );

        for (State state : apcStates) {
            when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)).thenReturn(Optional.of(state));
            assertTrue(homeOfficeRemoveRepresentationPersonalisation.getRecipientsList(asylumCase).contains(apcPrivateBetaInboxHomeOfficeEmailAddress));
        }
    }

    @Test
    void should_return_lart_email_address_for_given_states() {

        List<State> lartStates = newArrayList(
            State.RESPONDENT_REVIEW,
            State.SUBMIT_HEARING_REQUIREMENTS,
            State.LISTING
        );

        for (State state : lartStates) {
            when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)).thenReturn(Optional.of(state));
            assertTrue(homeOfficeRemoveRepresentationPersonalisation.getRecipientsList(asylumCase).contains(respondentReviewDirectionEmail));
        }
    }

    @Test
    void should_return_pou_email_address_for_given_states() {

        List<State> pouStates = newArrayList(
            State.ADJOURNED,
            State.PREPARE_FOR_HEARING,
            State.FINAL_BUNDLING,
            State.PRE_HEARING,
            State.DECISION,
            State.DECIDED,
            State.FTPA_SUBMITTED,
            State.FTPA_DECIDED
        );

        for (State state : pouStates) {
            when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)).thenReturn(Optional.of(state));
            when(appealService.isAppealListed(asylumCase)).thenReturn(true);
            assertTrue(homeOfficeRemoveRepresentationPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeHearingCentreEmail));
        }

        for (State state : pouStates) {
            when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)).thenReturn(Optional.of(state));
            when(appealService.isAppealListed(asylumCase)).thenReturn(false);
            assertTrue(homeOfficeRemoveRepresentationPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmail));
        }
    }

    @Test
    void should_return_given_template_id() {
        when(appealService.isAppealListed(asylumCase)).thenReturn(false);

        assertEquals(templateIdBeforeListing, homeOfficeRemoveRepresentationPersonalisation.getTemplateId(asylumCase));

        when(appealService.isAppealListed(asylumCase)).thenReturn(true);

        assertEquals(templateIdAfterListing, homeOfficeRemoveRepresentationPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_REMOVE_REPRESENTATION_HOME_OFFICE",
            homeOfficeRemoveRepresentationPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            homeOfficeRemoveRepresentationPersonalisation.getPersonalisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    void should_return_correctly_formatted_company_address() {

        when(asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)).thenReturn(Optional.of(addressUk));

        assertEquals("A, B, C, D, E, F, G", homeOfficeRemoveRepresentationPersonalisation.formatCompanyAddress(asylumCase));
    }

    @Test
    void should_return_correctly_formatted_company_address_for_missing_fields() {

        AddressUk addressUk = new AddressUk(
            "",
            "",
            "",
            "",
            "",
            "",
            ""
        );

        when(asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)).thenReturn(Optional.of(addressUk));

        assertEquals("", homeOfficeRemoveRepresentationPersonalisation.formatCompanyAddress(asylumCase));
    }
}
