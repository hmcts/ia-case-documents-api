package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@RunWith(MockitoJUnitRunner.class)
public class LegalRepresentativeEditListingPersonalisationTest {

    @Mock Callback<AsylumCase> callback;
    @Mock AsylumCase asylumCase;
    @Mock PersonalisationProvider personalisationProvider;

    private static final String HEARING_CENTRE_ADDRESS = "hearingCentreAddress";
    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String iaCcdFrontendUrl = "http://localhost";
    private String legalRepEmailAddress = "legalRep@example.com";
    private String hearingCentreAddress = "some hearing centre address";

    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "someAriaListingReference";
    private String appellantGivenNames = "appellantGivenNames";
    private String appellantFamilyName = "appellantFamilyName";
    private String homeOfficeRefNumber = "homeOfficeRefNumber";

    private String hearingCentreNameBefore = HearingCentre.MANCHESTER.toString();
    private String requirementsVulnerabilities = "someRequirementsVulnerabilities";
    private String requirementsMultimedia = "someRequirementsMultimedia";
    private String requirementsInCamera = "someRequirementsInCamera";
    private String requirementsSingleSexCourt = "someRequirementsSingleSexCourt";
    private String requirementsOther = "someRequirementsOther";

    private LegalRepresentativeEditListingPersonalisation legalRepresentativeEditListingPersonalisation;

    @Before
    public void setup() {
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of(legalRepEmailAddress));

        legalRepresentativeEditListingPersonalisation = new LegalRepresentativeEditListingPersonalisation(
            templateId,
            personalisationProvider
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, legalRepresentativeEditListingPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_CASE_RE_LISTED_LEGAL_REPRESENTATIVE", legalRepresentativeEditListingPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(legalRepresentativeEditListingPersonalisation.getRecipientsList(asylumCase).contains(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeEditListingPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legalRepresentativeEmailAddress is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> legalRepresentativeEditListingPersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationMapWithGivenValues());

        Map<String, String> personalisation = legalRepresentativeEditListingPersonalisation.getPersonalisation(callback);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    public void should_return_personalisation_when_optional_fields_are_blank() {
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationMapWithBlankValues());

        Map<String, String> personalisation = legalRepresentativeEditListingPersonalisation.getPersonalisation(callback);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        return ImmutableMap
            .<String, String>builder()
            .put("Hyperlink to user’s case list", iaCcdFrontendUrl)
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
            .put("oldHearingCentre", hearingCentreNameBefore)
            .put(HEARING_CENTRE_ADDRESS, hearingCentreAddress)
            .build();
    }

    private Map<String, String> getPersonalisationMapWithBlankValues() {
        return ImmutableMap
            .<String, String>builder()
            .put("Hyperlink to user’s case list", iaCcdFrontendUrl)
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
            .put(HEARING_CENTRE_ADDRESS, "")
            .build();
    }
}
