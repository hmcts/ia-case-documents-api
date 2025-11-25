package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantSendPaymentReminderPersonalisationEmailTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    SystemDateProvider systemDateProvider;
    private AppellantSendPaymentReminderPersonalisationEmail appellantSendPaymentReminderPersonalisationEmail;
    private Long caseId = 12345L;
    private int daysAfterNotification = 7;
    private String appealReferenceNumber = "someReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String templateId = "templateId";
    private String customerServicesPhone = "0100000000";
    private String customerServicesEmail = "services@email.com";
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private String appellantEmail = "test@test.com";
    private final String feeAmount = "14000";
    private String ccdReferenceNumber = "1111 2222 3333 4444";
    private Map<String, String> customerServices = Map.of("customerServicesTelephone", customerServicesPhone,
        "customerServicesEmail", customerServicesEmail);

    @BeforeEach
    public void setUp() {
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(customerServicesProvider.getCustomerServicesPersonalisation()).thenReturn(customerServices);
        when(asylumCase.read(INTERNAL_APPELLANT_EMAIL, String.class)).thenReturn(Optional.ofNullable(appellantEmail));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(ccdReferenceNumber));
        when(asylumCase.read(AsylumCaseDefinition.FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmount));

        appellantSendPaymentReminderPersonalisationEmail = new AppellantSendPaymentReminderPersonalisationEmail(
            templateId,
            daysAfterNotification,
            systemDateProvider,
            customerServicesProvider
        );
    }

    @Test
    public void should_return_given_email() {
        assertEquals(Collections.singleton(appellantEmail),
            appellantSendPaymentReminderPersonalisationEmail.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId,
            appellantSendPaymentReminderPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_INTERNAL_PAYMENT_REMINDER_APPELLANT_EMAIL",
            appellantSendPaymentReminderPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        final String dueDate = LocalDate.now().plusDays(daysAfterNotification)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));
        when(systemDateProvider.dueDate(daysAfterNotification)).thenReturn(dueDate);

        Map<String, String> personalisation =
            appellantSendPaymentReminderPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(customerServicesPhone, personalisation.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, personalisation.get("customerServicesEmail"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(ccdReferenceNumber, personalisation.get("onlineCaseReferenceNumber"));
        assertEquals("140.00", personalisation.get("feeAmount"));
        assertEquals(dueDate, personalisation.get("dueDate"));
    }

    @Test
    public void should_throw_exception_when_callback_is_null() {

        assertThatThrownBy(
            () -> appellantSendPaymentReminderPersonalisationEmail.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }
}
