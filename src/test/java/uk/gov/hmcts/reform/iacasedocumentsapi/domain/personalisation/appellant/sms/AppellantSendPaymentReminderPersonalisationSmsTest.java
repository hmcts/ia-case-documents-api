package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms.AppellantSendPaymentReminderPersonalisationSms;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantSendPaymentReminderPersonalisationSmsTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    SystemDateProvider systemDateProvider;
    private AppellantSendPaymentReminderPersonalisationSms appellantSendPaymentReminderPersonalisationSms;
    private Long caseId = 12345L;
    private int daysAfterNotification = 7;
    private String appealReferenceNumber = "someReferenceNumber";
    private String templateId = "templateId";
    private String appellantMobileNumber = "07781122334";
    private final String feeAmount = "14000";
    private String ccdReferenceNumber = "1111 2222 3333 4444";

    @BeforeEach
    public void setUp() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(INTERNAL_APPELLANT_MOBILE_NUMBER, String.class)).thenReturn(Optional.ofNullable(appellantMobileNumber));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(ccdReferenceNumber));
        when(asylumCase.read(AsylumCaseDefinition.FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmount));

        appellantSendPaymentReminderPersonalisationSms = new AppellantSendPaymentReminderPersonalisationSms(
            templateId,
            daysAfterNotification,
            systemDateProvider
        );
    }

    @Test
    public void should_return_given_email() {
        assertEquals(Collections.singleton(appellantMobileNumber),
            appellantSendPaymentReminderPersonalisationSms.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId,
            appellantSendPaymentReminderPersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_INTERNAL_PAYMENT_REMINDER_APPELLANT_SMS",
            appellantSendPaymentReminderPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_personalisation_when_all_information_given_in_country() {
        final String dueDate = LocalDate.now().plusDays(daysAfterNotification)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));
        when(systemDateProvider.dueDate(daysAfterNotification)).thenReturn(dueDate);

        Map<String, String> personalisation =
            appellantSendPaymentReminderPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(ccdReferenceNumber, personalisation.get("onlineCaseReferenceNumber"));
        assertEquals("140.00", personalisation.get("feeAmount"));
        assertEquals(dueDate, personalisation.get("dueDate"));
    }

    @Test
    public void should_throw_exception_when_callback_is_null() {

        assertThatThrownBy(
            () -> appellantSendPaymentReminderPersonalisationSms.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }
}
