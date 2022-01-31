package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

@ExtendWith(MockitoExtension.class)
class AdminOfficerDecidedOrEndedAppealPendingPaymentTest {


    @Mock
    private AsylumCase asylumCase;
    @Mock
    private AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;
    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String ctscAdminPendingPaymentEmailAddress = "pendingPayment@example.com";

    private AdminOfficerDecidedOrEndedAppealPendingPayment adminOfficerDecidedOrEndedAppealPendingPayment;

    @BeforeEach
    void setUp() {

        adminOfficerDecidedOrEndedAppealPendingPayment =
                new AdminOfficerDecidedOrEndedAppealPendingPayment(
                        templateId, ctscAdminPendingPaymentEmailAddress, adminOfficerPersonalisationProvider);
    }

    @Test
    public void should_return_given_template_id() {
        assertThat(adminOfficerDecidedOrEndedAppealPendingPayment.getTemplateId()).isEqualTo(templateId);
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
                () -> adminOfficerDecidedOrEndedAppealPendingPayment.getPersonalisation((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_given_reference_id() {
        assertThat(adminOfficerDecidedOrEndedAppealPendingPayment.getReferenceId(caseId))
                .isEqualTo(caseId + "_APPEAL_PENDING_PAYMENT_ADMIN_OFFICER");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
                adminOfficerDecidedOrEndedAppealPendingPayment.getPersonalisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        Map<String, String> personalisation =
                adminOfficerDecidedOrEndedAppealPendingPayment.getPersonalisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }
}
