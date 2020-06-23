package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

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

@RunWith(MockitoJUnitRunner.class)
public class AdminOfficerAdjournHearingWithoutDatePersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    private String templateId = "someTemplateId";

    private String adminOfficerEmailAddress = "adminOfficer@example.com";

    private AdminOfficerAdjournHearingWithoutDatePersonalisation adminOfficerdjournHearingWithoutDatePersonalisation;

    @Before
    public void setup() {
        String appealReferenceNumber = "someReferenceNumber";
        String appellantGivenNames = "someAppellantGivenNames";
        String appellantFamilyName = "someAppellantFamilyName";
        String listRef = "LP/12345/2019";
        when(adminOfficerPersonalisationProvider.getChangeToHearingRequirementsPersonalisation(asylumCase)).thenReturn(ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("ariaListingReference", listRef)
            .build());

        adminOfficerdjournHearingWithoutDatePersonalisation = new AdminOfficerAdjournHearingWithoutDatePersonalisation(templateId, adminOfficerEmailAddress, adminOfficerPersonalisationProvider);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, adminOfficerdjournHearingWithoutDatePersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_ADJOURN_HEARING_WITHOUT_DATE_ADMIN_OFFICER", adminOfficerdjournHearingWithoutDatePersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(adminOfficerdjournHearingWithoutDatePersonalisation.getRecipientsList(asylumCase).contains(adminOfficerEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> adminOfficerdjournHearingWithoutDatePersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = adminOfficerdjournHearingWithoutDatePersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation).isEqualToComparingOnlyGivenFields(asylumCase);
    }
}
