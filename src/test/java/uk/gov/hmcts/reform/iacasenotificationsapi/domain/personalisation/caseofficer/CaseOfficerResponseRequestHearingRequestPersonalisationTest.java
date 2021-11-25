package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CaseOfficerResponseRequestHearingRequestPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    Map<HearingCentre, String> hearingCentreEmailAddressMap;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    private FeatureToggler featureToggler;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String iaExUiFrontendUrl = "http://somefrontendurl";
    private HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private String hearingCentreEmailAddress = "hearingCentre@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private CaseOfficerRequestHearingRequirementsPersonalisation caseOfficerRequestHearingRequirementsPersonalisation;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(hearingCentreEmailAddressMap.get(hearingCentre)).thenReturn(hearingCentreEmailAddress);

        caseOfficerRequestHearingRequirementsPersonalisation = new CaseOfficerRequestHearingRequirementsPersonalisation(
            templateId,
            iaExUiFrontendUrl,
            hearingCentreEmailAddressMap,
            customerServicesProvider,
                featureToggler);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, caseOfficerRequestHearingRequirementsPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_CASE_OFFICER_REQUEST_HEARING_REQUIREMENTS_DIRECTION",
            caseOfficerRequestHearingRequirementsPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_lookup_map_when_feature_flag_is_Off() {
        assertTrue(caseOfficerRequestHearingRequirementsPersonalisation.getRecipientsList(asylumCase)
                .isEmpty());
    }

    @Test
    public void should_return_given_email_address_from_lookup_map_when_feature_flag_is_On() {
        when(featureToggler.getValue("tcw-notifications-feature", true)).thenReturn(true);
        assertTrue(caseOfficerRequestHearingRequirementsPersonalisation.getRecipientsList(asylumCase)
                .contains(hearingCentreEmailAddress));
    }


    @Test
    public void should_throw_exception_on_email_address_when_hearing_centre_is_empty() {
        when(featureToggler.getValue("tcw-notifications-feature", true)).thenReturn(true);
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> caseOfficerRequestHearingRequirementsPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("hearingCentre is not present");
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_hearing_centre() {
        when(featureToggler.getValue("tcw-notifications-feature", true)).thenReturn(true);
        when(hearingCentreEmailAddressMap.get(hearingCentre)).thenReturn(null);
        assertThatThrownBy(() -> caseOfficerRequestHearingRequirementsPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Hearing centre email address not found: " + hearingCentre.toString());
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> caseOfficerRequestHearingRequirementsPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            caseOfficerRequestHearingRequirementsPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            caseOfficerRequestHearingRequirementsPersonalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
    }
}
