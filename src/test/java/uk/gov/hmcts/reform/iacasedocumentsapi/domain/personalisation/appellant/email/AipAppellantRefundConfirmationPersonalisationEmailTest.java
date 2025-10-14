package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email.AipAppellantRefundConfirmationPersonalisationEmail;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AipAppellantRefundConfirmationPersonalisationEmailTest {

    private Long caseId = 12345L;
    private String refundConfirmationInCountryTemplateId = "refundConfirmationInCountryTemplateId";
    private String refundConfirmationOutOfCountryTemplateId = "refundConfirmationOutOfCountryTemplateId";
    private String iaAipFrontendUrl = "http://localhost";
    private String appealReferenceNumber = "appealReferenceNumber";
    private String homeOfficeReferenceNumber = "homeOfficeReferenceNumber";
    private String appellantGivenNames = "GivenNames";
    private String appellantFamilyName = "FamilyName";
    private String appellantEmail = "test@email.com";
    private int daysAfterRemissionDecision = 14;
    private String newFeeAmount = "8000";
    private String withHearing = "decisionWithHearing";
    private String withoutHearing = "decisionWithoutHearing";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    @Mock
    AsylumCase asylumCase;
    @Mock
    SystemDateProvider systemDateProvider;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    RecipientsFinder recipientsFinder;

    private AipAppellantRefundConfirmationPersonalisationEmail aipAppellantRefundConfirmationPersonalisationEmail;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(NEW_FEE_AMOUNT, String.class)).thenReturn(Optional.of(newFeeAmount));
        when(asylumCase.read(PREVIOUS_DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of(withHearing));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of(withoutHearing));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        aipAppellantRefundConfirmationPersonalisationEmail = new AipAppellantRefundConfirmationPersonalisationEmail(
            refundConfirmationInCountryTemplateId,
            refundConfirmationOutOfCountryTemplateId,
            iaAipFrontendUrl,
            daysAfterRemissionDecision,
            customerServicesProvider,
            recipientsFinder,
            systemDateProvider
            );
    }

    @Test
    void should_return_correct_template_id_with_or_without_home_office_reference() {
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        assertTrue(aipAppellantRefundConfirmationPersonalisationEmail.getTemplateId(asylumCase).contains(refundConfirmationInCountryTemplateId));

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        assertTrue(aipAppellantRefundConfirmationPersonalisationEmail.getTemplateId(asylumCase).contains(refundConfirmationOutOfCountryTemplateId));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_REFUND_CONFIRMATION_AIP_APPELLANT_EMAIL",
            aipAppellantRefundConfirmationPersonalisationEmail.getReferenceId(12345L));
    }

    @Test
    void should_return_appellant_email_address_from_asylum_case() {
        Mockito.when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(appellantEmail));

        assertTrue(aipAppellantRefundConfirmationPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(appellantEmail));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(
            () -> aipAppellantRefundConfirmationPersonalisationEmail.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        final String dueDate = LocalDate.now().plusDays(daysAfterRemissionDecision)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(systemDateProvider.dueDate(daysAfterRemissionDecision)).thenReturn(dueDate);

        Map<String, String> personalisation =
            aipAppellantRefundConfirmationPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("Decision with hearing", personalisation.get("previousDecisionHearingFeeOption"));
        assertEquals("Decision without hearing", personalisation.get("updatedDecisionHearingFeeOption"));
        assertEquals("80.00", personalisation.get("newFee"));
        assertEquals(systemDateProvider.dueDate(daysAfterRemissionDecision), personalisation.get("dueDate"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

}
