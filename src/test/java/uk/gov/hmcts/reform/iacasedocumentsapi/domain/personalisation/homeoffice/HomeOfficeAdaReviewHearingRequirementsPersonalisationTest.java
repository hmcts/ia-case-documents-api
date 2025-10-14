package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.homeoffice;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.homeoffice.HomeOfficeAdaReviewHearingRequirementsPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HomeOfficeAdaReviewHearingRequirementsPersonalisationTest {

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
    HearingDetailsFinder hearingDetailsFinder;

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

    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "someAriaListingReference";
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private String caseOfficerReviewedVulnerabilities = "someCaseOfficerReviewedVulnerabilities";
    private String caseOfficerReviewedMultimedia = "someCaseOfficerReviewedMultimedia";
    private String caseOfficerReviewedSingleSexCourt = "someCaseOfficerReviewedSingleSexCourt";
    private String caseOfficerReviewedInCamera = "someCaseOfficerReviewedInCamera";
    private String caseOfficerReviewedOther = "someCaseOfficerReviewedOther";

    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    private HomeOfficeAdaReviewHearingRequirementsPersonalisation homeOfficeAdaReviewHearingRequirementsPersonalisation;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDateTime));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

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
        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.YES));

        when(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase)).thenReturn(homeOfficeEmailAddress);

        when(hearingDetailsFinder.getHearingDateTime(asylumCase)).thenReturn(hearingDateTime);
        when(hearingDetailsFinder.getHearingCentreName(asylumCase)).thenReturn(hearingCentre.toString());
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCase)).thenReturn(hearingCentreAddress);
        when(stringProvider.get("hearingCentreAddress", hearingCentre.toString()))
            .thenReturn(Optional.of(hearingCentreAddress));
        when(dateTimeExtractor.extractHearingDate(hearingDateTime)).thenReturn(hearingDate);
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(hearingDetailsFinder.getHearingCentreLocation(asylumCase)).thenReturn(hearingCentreAddress);

        homeOfficeAdaReviewHearingRequirementsPersonalisation = new HomeOfficeAdaReviewHearingRequirementsPersonalisation(
            templateId,
            iaExUiFrontendUrl,
            dateTimeExtractor,
            emailAddressFinder,
            customerServicesProvider,
            hearingDetailsFinder
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, homeOfficeAdaReviewHearingRequirementsPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_HEARING_REQUIREMENTS_AGREED_HOME_OFFICE", homeOfficeAdaReviewHearingRequirementsPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_lookup_map() {
        assertTrue(homeOfficeAdaReviewHearingRequirementsPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmailAddress));
    }

    @Test
    void should_return_personalisation_when_all_information_given_in_remote_hearing_case() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(remoteHearingCentre));

        when(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase)).thenReturn(hearingCentreEmailAddress);

        when(hearingDetailsFinder.getHearingCentreLocation(asylumCase))
            .thenReturn(remoteHearingCentreAddress);

        Map<String, String> personalisation = homeOfficeAdaReviewHearingRequirementsPersonalisation.getPersonalisation(asylumCase);

        assertEquals(remoteHearingCentreAddress, personalisation.get("hearingCentreAddress"));
    }

    @Test
    public void should_return_personalisation_when_co_records_hearing_response() {
        Map<String, String> personalisation = homeOfficeAdaReviewHearingRequirementsPersonalisation.getPersonalisation(asylumCase);

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

        when(asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation = homeOfficeAdaReviewHearingRequirementsPersonalisation.getPersonalisation(asylumCase);

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
        assertEquals(hearingCentreAddress, personalisation.get("hearingCentreAddress"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }
}
