package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HomeOfficeListCasePersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    StringProvider stringProvider;
    @Mock
    DateTimeExtractor dateTimeExtractor;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    AppealService appealService;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String iaExUiFrontendUrl = "http://somefrontendurl";
    private HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private String homeOfficeEmailAddress = "homeoffice@example.com";
    private String hearingCentreAddress = "some hearing centre address";
    //Remote hearing
    private HearingCentre remoteHearingCentre = HearingCentre.REMOTE_HEARING;
    private String hearingCentreEmailAddress = "taylorHouseHearingCentre@example.com";
    private String remoteHearingCentreAddress = "hearing centre address";

    private String hearingDateTime = "2019-08-27T14:25:15.000";
    private String hearingDate = "2019-08-27";
    private String hearingTime = "14:25";

    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "someAriaListingReference";
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private String requirementsVulnerabilities = "someRequirementsVulnerabilities";
    private String requirementsMultimedia = "someRequirementsMultimedia";
    private String requirementsSingleSexCourt = "someRequirementsSingleSexCourt";
    private String requirementsInCamera = "someRequirementsInCamera";
    private String requirementsOther = "someRequirementsOther";

    private String caseOfficerReviewedVulnerabilities = "someCaseOfficerReviewedVulnerabilities";
    private String caseOfficerReviewedMultimedia = "someCaseOfficerReviewedMultimedia";
    private String caseOfficerReviewedSingleSexCourt = "someCaseOfficerReviewedSingleSexCourt";
    private String caseOfficerReviewedInCamera = "someCaseOfficerReviewedInCamera";
    private String caseOfficerReviewedOther = "someCaseOfficerReviewedOther";

    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    private HomeOfficeListCasePersonalisation homeOfficeListCasePersonalisation;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDateTime));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when(asylumCase.read(LIST_CASE_REQUIREMENTS_VULNERABILITIES, String.class))
            .thenReturn(Optional.of(requirementsVulnerabilities));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_MULTIMEDIA, String.class))
            .thenReturn(Optional.of(requirementsMultimedia));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT, String.class))
            .thenReturn(Optional.of(requirementsSingleSexCourt));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT, String.class))
            .thenReturn(Optional.of(requirementsInCamera));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_OTHER, String.class)).thenReturn(Optional.of(requirementsOther));

        when(asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(caseOfficerReviewedVulnerabilities));
        when(asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(caseOfficerReviewedMultimedia));
        when(asylumCase.read(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(caseOfficerReviewedSingleSexCourt));
        when(asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(caseOfficerReviewedInCamera));
        when(asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(caseOfficerReviewedOther));
        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.NO));

        when(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase)).thenReturn(homeOfficeEmailAddress);
        when(stringProvider.get("hearingCentreAddress", hearingCentre.toString()))
            .thenReturn(Optional.of(hearingCentreAddress));
        when(dateTimeExtractor.extractHearingDate(hearingDateTime)).thenReturn(hearingDate);
        when(dateTimeExtractor.extractHearingTime(hearingDateTime)).thenReturn(hearingTime);

        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        homeOfficeListCasePersonalisation = new HomeOfficeListCasePersonalisation(
            templateId,
            iaExUiFrontendUrl,
            stringProvider,
            dateTimeExtractor,
            emailAddressFinder,
            customerServicesProvider,
            appealService
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, homeOfficeListCasePersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_CASE_LISTED_HOME_OFFICE", homeOfficeListCasePersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_lookup_map() {
        assertTrue(homeOfficeListCasePersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmailAddress));
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = homeOfficeListCasePersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(requirementsVulnerabilities, personalisation.get("hearingRequirementVulnerabilities"));
        assertEquals(requirementsMultimedia, personalisation.get("hearingRequirementMultimedia"));
        assertEquals(requirementsSingleSexCourt, personalisation.get("hearingRequirementSingleSexCourt"));
        assertEquals(requirementsInCamera, personalisation.get("hearingRequirementInCameraCourt"));
        assertEquals(requirementsOther, personalisation.get("hearingRequirementOther"));
        assertEquals(hearingDate, personalisation.get("hearingDate"));
        assertEquals(hearingTime, personalisation.get("hearingTime"));
        assertEquals(hearingCentreAddress, personalisation.get("hearingCentreAddress"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    void should_return_personalisation_when_all_information_given_in_remote_hearing_case() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(remoteHearingCentre));

        when(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase)).thenReturn(hearingCentreEmailAddress);
        when(stringProvider.get("hearingCentreAddress", remoteHearingCentre.toString()))
            .thenReturn(Optional.of(remoteHearingCentreAddress));

        Map<String, String> personalisation = homeOfficeListCasePersonalisation.getPersonalisation(asylumCase);

        assertEquals(remoteHearingCentreAddress, personalisation.get("hearingCentreAddress"));
    }


    @Test
    void should_throw_exception_on_personalisation_when_hearing_centre_is_empty_in_remote_hearing() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.REMOTE_HEARING));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> homeOfficeListCasePersonalisation.getPersonalisation(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("hearingCentreAddress is not present");
    }

    @Test
    void should_throw_exception_on_personalisation_when_hearing_centre_address_does_not_exist_in_remote_hearing() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.REMOTE_HEARING));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(stringProvider.get("hearingCentreAddress", hearingCentre.toString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> homeOfficeListCasePersonalisation.getPersonalisation(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("hearingCentreAddress is not present");
    }

    @Test
    public void should_return_personalisation_when_co_records_hearing_response() {

        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.YES));
        Map<String, String> personalisation = homeOfficeListCasePersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(caseOfficerReviewedVulnerabilities, personalisation.get("hearingRequirementVulnerabilities"));
        assertEquals(caseOfficerReviewedMultimedia, personalisation.get("hearingRequirementMultimedia"));
        assertEquals(caseOfficerReviewedSingleSexCourt, personalisation.get("hearingRequirementSingleSexCourt"));
        assertEquals(caseOfficerReviewedInCamera, personalisation.get("hearingRequirementInCameraCourt"));
        assertEquals(caseOfficerReviewedOther, personalisation.get("hearingRequirementOther"));
        assertEquals(hearingDate, personalisation.get("hearingDate"));
        assertEquals(hearingTime, personalisation.get("hearingTime"));
        assertEquals(hearingCentreAddress, personalisation.get("hearingCentreAddress"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(LIST_CASE_REQUIREMENTS_VULNERABILITIES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_MULTIMEDIA, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_OTHER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation = homeOfficeListCasePersonalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("ariaListingReference"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals("No special adjustments are being made to accommodate vulnerabilities",
            personalisation.get("hearingRequirementVulnerabilities"));
        assertEquals("No multimedia equipment is being provided", personalisation.get("hearingRequirementMultimedia"));
        assertEquals("The court will not be single sex", personalisation.get("hearingRequirementSingleSexCourt"));
        assertEquals("The hearing will be held in public court",
            personalisation.get("hearingRequirementInCameraCourt"));
        assertEquals("No other adjustments are being made", personalisation.get("hearingRequirementOther"));
        assertEquals(hearingDate, personalisation.get("hearingDate"));
        assertEquals(hearingTime, personalisation.get("hearingTime"));
        assertEquals(hearingCentreAddress, personalisation.get("hearingCentreAddress"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    public void should_throw_exception_on_personalisation_when_hearing_centre_is_empty() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> homeOfficeListCasePersonalisation.getPersonalisation(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("listCaseHearingCentre is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_hearing_centre_address_does_not_exist() {

        when(stringProvider.get("hearingCentreAddress", hearingCentre.toString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> homeOfficeListCasePersonalisation.getPersonalisation(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("hearingCentreAddress is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_hearing_date_time_does_not_exist() {

        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> homeOfficeListCasePersonalisation.getPersonalisation(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("hearingDateTime is not present");
    }
}
