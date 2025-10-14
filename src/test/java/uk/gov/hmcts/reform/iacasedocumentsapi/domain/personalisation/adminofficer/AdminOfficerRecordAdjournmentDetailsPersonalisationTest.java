package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.adminofficer;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.adminofficer.AdminOfficerPersonalisationProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.adminofficer.AdminOfficerRecordAdjournmentDetailsPersonalisation;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AdminOfficerRecordAdjournmentDetailsPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    private String templateId = "someTemplateId";

    private String adminOfficerEmailAddress = "adminOfficer@example.com";

    private AdminOfficerRecordAdjournmentDetailsPersonalisation adminOfficerRecordAdjournmentDetailsPersonalisation;

    @BeforeEach
    public void setup() {
        String appealReferenceNumber = "someReferenceNumber";
        String appellantGivenNames = "someAppellantGivenNames";
        String appellantFamilyName = "someAppellantFamilyName";
        when(adminOfficerPersonalisationProvider.getChangeToHearingRequirementsPersonalisation(asylumCase))
            .thenReturn(ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", appealReferenceNumber)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantFamilyName)
                .build());

        adminOfficerRecordAdjournmentDetailsPersonalisation =
            new AdminOfficerRecordAdjournmentDetailsPersonalisation(templateId, adminOfficerEmailAddress,
                adminOfficerPersonalisationProvider);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, adminOfficerRecordAdjournmentDetailsPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_RECORD_ADJOURNMENT_DETAILS_ADMIN_OFFICER",
            adminOfficerRecordAdjournmentDetailsPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(adminOfficerRecordAdjournmentDetailsPersonalisation.getRecipientsList(asylumCase)
            .contains(adminOfficerEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> adminOfficerRecordAdjournmentDetailsPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            adminOfficerRecordAdjournmentDetailsPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation).isEqualToComparingOnlyGivenFields(asylumCase);
    }
}
