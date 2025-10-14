package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.homeoffice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.homeoffice.HomeOfficeEditListingPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HomeOfficeEditListingPersonalisationTest {

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
    HearingDetailsFinder hearingDetailsFinder;

    private Long caseId = 12345L;

    private String adaTemplateId = "adaTemplateId";
    private String nonAdaTemplateId = "nonAdaTemplateId";
    private String listAssistHearingTemplateId = "listAssistHearingTemplateId";
    private String iaExUiFrontendUrl = "http://localhost";
    private String homeOfficeEmailAddress = "homeoffice@example.com";
    private String listCaseHomeOfficeEmailAddress = "listCaseHomeoOffice@example.com";
    private String hearingCentreName = "The Hearing Centre";

    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "someAriaListingReference";
    private String appellantGivenNames = "appellantGivenNames";
    private String appellantFamilyName = "appellantFamilyName";
    private String homeOfficeRefNumber = "homeOfficeRefNumber";

    private String requirementsVulnerabilities = "someRequirementsVulnerabilities";
    private String requirementsMultimedia = "someRequirementsMultimedia";
    private String requirementsSingleSexCourt = "someRequirementsSingleSexCourt";
    private String requirementsInCamera = "someRequirementsInCamera";
    private String requirementsOther = "someRequirementsOther";

    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";
    private String hearingCentreAddress = "hearingCentreAddress";

    private HomeOfficeEditListingPersonalisation homeOfficeEditListingPersonalisation;

    @BeforeEach
    public void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase)).thenReturn(listCaseHomeOfficeEmailAddress);
        when(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase)).thenReturn(homeOfficeEmailAddress);

        homeOfficeEditListingPersonalisation = new HomeOfficeEditListingPersonalisation(
            nonAdaTemplateId,
            adaTemplateId,
            listAssistHearingTemplateId,
            emailAddressFinder,
            personalisationProvider,
            customerServicesProvider,
            hearingDetailsFinder
        );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(nonAdaTemplateId, homeOfficeEditListingPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertEquals(adaTemplateId, homeOfficeEditListingPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_CASE_RE_LISTED_HOME_OFFICE",
            homeOfficeEditListingPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_lookup_map() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

        assertTrue(homeOfficeEditListingPersonalisation.getRecipientsList(asylumCase).contains(listCaseHomeOfficeEmailAddress));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.REMOTE_HEARING));

        assertTrue(homeOfficeEditListingPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmailAddress));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(() -> homeOfficeEditListingPersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationMapWithGivenValues());
        when(hearingDetailsFinder.getHearingCentreLocation(callback.getCaseDetails().getCaseData()))
                .thenReturn(hearingCentreAddress);

        Map<String, String> personalisation = homeOfficeEditListingPersonalisation.getPersonalisation(callback);

        assertThat(personalisation).isNotEmpty();
        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("ariaListingReference", ariaListingReference)
            .put("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("hearingCentreName", hearingCentreName)
            .put("hearingRequirementVulnerabilities", requirementsVulnerabilities)
            .put("hearingRequirementMultimedia", requirementsMultimedia)
            .put("hearingRequirementSingleSexCourt", requirementsSingleSexCourt)
            .put("hearingRequirementInCameraCourt", requirementsInCamera)
            .put("hearingRequirementOther", requirementsOther)
            .put("customerServicesTelephone", customerServicesTelephone)
            .put("customerServicesEmail", customerServicesEmail)
            .build();
    }

    private Map<String, String> getPersonalisationMapWithBlankValues() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", "")
            .put("ariaListingReference", "")
            .put("homeOfficeReferenceNumber", "")
            .put("appellantGivenNames", "")
            .put("appellantFamilyName", "")
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("hearingCentreName", "")
            .put("hearingRequirementVulnerabilities", "")
            .put("hearingRequirementMultimedia", "")
            .put("hearingRequirementSingleSexCourt", "")
            .put("hearingRequirementInCameraCourt", "")
            .put("hearingRequirementOther", "")
            .put("oldHearingCentre", "")
            .put("customerServicesTelephone", "")
            .put("customerServicesEmail", "")
            .build();
    }
}
