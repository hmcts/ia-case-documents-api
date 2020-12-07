package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.REMISSION_TYPE;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionType;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AdminOfficerAppealSubmittedPendingPaymentPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    private String templateId = "someTemplateId";
    private String remissionTemplateId = "someRemissionTemplateId";
    private String appealReferenceNumber = "someReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String listRef = "LP/12345/2019";
    private String adminOfficerEmailAddress = "adminOfficer@example.com";
    private String paymentExceptionsAdminOfficerEmailAddress = "payment-exceptions-ao@example.com";
    private String iaExUiFrontendUrl = "http://localhost";

    private AdminOfficerAppealSubmittedPendingPaymentPersonalisation
        adminOfficerAppealSubmittedPendingPaymentPersonalisation;

    @BeforeEach
    public void setup() {
        when(adminOfficerPersonalisationProvider.getChangeToHearingRequirementsPersonalisation(asylumCase))
            .thenReturn(ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", appealReferenceNumber)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantFamilyName)
                .put("ariaListingReference", listRef)
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build());

        adminOfficerAppealSubmittedPendingPaymentPersonalisation =
            new AdminOfficerAppealSubmittedPendingPaymentPersonalisation(
                templateId,
                remissionTemplateId,
                adminOfficerEmailAddress,
                paymentExceptionsAdminOfficerEmailAddress,
                adminOfficerPersonalisationProvider);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, adminOfficerAppealSubmittedPendingPaymentPersonalisation.getTemplateId(asylumCase));
    }

    @ParameterizedTest
    @EnumSource(
        value = RemissionType.class,
        names = {"HO_WAIVER_REMISSION", "HELP_WITH_FEES", "EXCEPTIONAL_CIRCUMSTANCES_REMISSION"})
    public void should_return_given_template_id_with_remission(RemissionType remissionType) {
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(remissionType));
        assertEquals(
            remissionTemplateId, adminOfficerAppealSubmittedPendingPaymentPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_APPEAL_SUBMITTED_PENDING_PAYMENT_ADMIN_OFFICER",
            adminOfficerAppealSubmittedPendingPaymentPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(adminOfficerAppealSubmittedPendingPaymentPersonalisation.getRecipientsList(asylumCase)
            .contains(adminOfficerEmailAddress));
    }

    @ParameterizedTest
    @EnumSource(
        value = RemissionType.class,
        names = {"HO_WAIVER_REMISSION", "HELP_WITH_FEES", "EXCEPTIONAL_CIRCUMSTANCES_REMISSION"})
    public void should_return_payment_email_address_with_remission(RemissionType remissionType) {
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(remissionType));
        assertTrue(adminOfficerAppealSubmittedPendingPaymentPersonalisation.getRecipientsList(asylumCase)
            .contains(paymentExceptionsAdminOfficerEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> adminOfficerAppealSubmittedPendingPaymentPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            adminOfficerAppealSubmittedPendingPaymentPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
    }
}
