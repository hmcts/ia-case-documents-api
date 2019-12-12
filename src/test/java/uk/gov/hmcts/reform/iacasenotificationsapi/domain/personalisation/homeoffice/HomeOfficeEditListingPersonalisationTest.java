package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.BasePersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@RunWith(MockitoJUnitRunner.class)
public class HomeOfficeEditListingPersonalisationTest {

    @Mock Callback<AsylumCase> callback;
    @Mock AsylumCase asylumCase;
    @Mock EmailAddressFinder emailAddressFinder;
    @Mock BasePersonalisationProvider basePersonalisationProvider;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";

    private String homeOfficeEmailAddress = "homeoffice@example.com";

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

    private HomeOfficeEditListingPersonalisation homeOfficeEditListingPersonalisation;

    @Before
    public void setup() {
        when(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase)).thenReturn(homeOfficeEmailAddress);

        homeOfficeEditListingPersonalisation = new HomeOfficeEditListingPersonalisation(
            templateId,
            emailAddressFinder,
            basePersonalisationProvider
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, homeOfficeEditListingPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_CASE_RE_LISTED_HOME_OFFICE", homeOfficeEditListingPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_lookup_map() {
        assertTrue(homeOfficeEditListingPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> homeOfficeEditListingPersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        when(basePersonalisationProvider.getEditCaseListingPersonalisation(callback)).thenReturn(getPersonalisationMapWithGivenValues());

        Map<String, String> personalisation = homeOfficeEditListingPersonalisation.getPersonalisation(callback);

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
            .put("Hearing Requirement Vulnerabilities", requirementsVulnerabilities)
            .put("Hearing Requirement Multimedia", requirementsMultimedia)
            .put("Hearing Requirement Single Sex Court", requirementsSingleSexCourt)
            .put("Hearing Requirement In Camera Court", requirementsInCamera)
            .put("Hearing Requirement Other", requirementsOther)
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
            .put("Hearing Requirement Vulnerabilities", "")
            .put("Hearing Requirement Multimedia", "")
            .put("Hearing Requirement Single Sex Court", "")
            .put("Hearing Requirement In Camera Court", "")
            .put("Hearing Requirement Other", "")
            .put("oldHearingCentre", "")
            .build();
    }
}
