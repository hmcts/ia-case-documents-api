package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.adminofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.adminofficer.AdminOfficerAdjournHearingWithoutDateNonDetainedPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.adminofficer.AdminOfficerPersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminOfficerAdjournHearingWithoutDateNonDetainedPersonalisationTest {
    @Mock
    AsylumCase asylumCase;
    @Mock
    AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    private String templateId = "someTemplateId";

    private String adminOfficerEmailAddress = "adminOfficer@example.com";

    private AdminOfficerAdjournHearingWithoutDateNonDetainedPersonalisation adminOfficerAdjournHearingWithoutDateNonDetainedPersonalisation;

    @BeforeEach
    public void setup() {
        String appealReferenceNumber = "someReferenceNumber";
        String appellantGivenNames = "someAppellantGivenNames";
        String appellantFamilyName = "someAppellantFamilyName";
        when(adminOfficerPersonalisationProvider.getDefaultPersonalisation(asylumCase))
            .thenReturn(ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", appealReferenceNumber)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantFamilyName)
                .build());

        adminOfficerAdjournHearingWithoutDateNonDetainedPersonalisation =
            new AdminOfficerAdjournHearingWithoutDateNonDetainedPersonalisation(templateId, adminOfficerEmailAddress,
                adminOfficerPersonalisationProvider);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, adminOfficerAdjournHearingWithoutDateNonDetainedPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_ADJOURN_HEARING_WITHOUT_DATE_ADMIN_OFFICER",
            adminOfficerAdjournHearingWithoutDateNonDetainedPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(adminOfficerAdjournHearingWithoutDateNonDetainedPersonalisation.getRecipientsList(asylumCase)
            .contains(adminOfficerEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> adminOfficerAdjournHearingWithoutDateNonDetainedPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        Map<String, String> personalisation =
            adminOfficerAdjournHearingWithoutDateNonDetainedPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation).isNotEmpty();
        assertThat(personalisation).isEqualToComparingOnlyGivenFields(asylumCase);
    }
}
