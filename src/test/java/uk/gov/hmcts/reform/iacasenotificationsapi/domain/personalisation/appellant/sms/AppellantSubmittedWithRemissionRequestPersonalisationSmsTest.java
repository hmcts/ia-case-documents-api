package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantSubmittedWithRemissionRequestPersonalisationSmsTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    SystemDateProvider systemDateProvider;

    private Long caseId = 12345L;
    private String smsTemplateId = "someSmsTemplateId";
    private String paPayLaterSmsTemplateId = "somePaPayLaterSmsTemplateId";
    private String iaAipFrontendUrl = "http://localhost";

    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantMobilePhone = "07123456789";

    private AppellantSubmittedWithRemissionRequestPersonalisationSms appellantSubmittedWithRemissionRequestPersonalisationSms;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));

        appellantSubmittedWithRemissionRequestPersonalisationSms = new AppellantSubmittedWithRemissionRequestPersonalisationSms(
            smsTemplateId,
            paPayLaterSmsTemplateId,
            14,
            iaAipFrontendUrl,
            14,
            recipientsFinder,
            systemDateProvider);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(smsTemplateId, appellantSubmittedWithRemissionRequestPersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_pa_pay_later_template_id_when_appellant_is_pa_and_payment_option_is_payLater() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payLater"));

        String templateId = appellantSubmittedWithRemissionRequestPersonalisationSms.getTemplateId(asylumCase);
        assertEquals(paPayLaterSmsTemplateId, templateId);
    }

    @Test
    public void should_return_pa_pay_later_template_id_when_appellant_is_pa_and_payment_option_is_payOffline() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));

        String templateId = appellantSubmittedWithRemissionRequestPersonalisationSms.getTemplateId(asylumCase);
        assertEquals(paPayLaterSmsTemplateId, templateId);
    }

    @Test
    public void should_return_default_template_id_when_appeal_type_is_not_pa() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));

        String templateId = appellantSubmittedWithRemissionRequestPersonalisationSms.getTemplateId(asylumCase);
        assertEquals(smsTemplateId, templateId);
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_SUBMITTED_WITH_REMISSION_REQUEST_AIP_SMS",
            appellantSubmittedWithRemissionRequestPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_sms_list_from_subscribers_in_asylum_case() {

        Subscriber subscriber = new Subscriber(
            SubscriberType.APPELLANT, //subscriberType
            "", //email
            YesOrNo.NO, // wants email
            mockedAppellantMobilePhone, //mobileNumber
            YesOrNo.YES // wants sms
        );

        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS)).thenCallRealMethod();
        when(asylumCase.read(SUBSCRIPTIONS))
            .thenReturn(Optional.of(Collections.singletonList(new IdValue<>("foo", subscriber))));

        assertTrue(appellantSubmittedWithRemissionRequestPersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS)).thenCallRealMethod();

        assertThatThrownBy(() -> appellantSubmittedWithRemissionRequestPersonalisationSms.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_throw_exception_when_asylum_case_is_null_for_personalisation() {
        assertThatThrownBy(() -> appellantSubmittedWithRemissionRequestPersonalisationSms.getPersonalisation((AsylumCase) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        final String dueDate = LocalDate.now().plusDays(14)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));
        when(systemDateProvider.dueDate(14)).thenReturn(dueDate);

        Map<String, String> personalisation =
            appellantSubmittedWithRemissionRequestPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(dueDate, personalisation.get("appealSubmittedDaysAfter"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));

    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {
        final String dueDate = LocalDate.now().plusDays(14)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));
        when(systemDateProvider.dueDate(14)).thenReturn(dueDate);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            appellantSubmittedWithRemissionRequestPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("Appeal Ref Number"));
        assertEquals(dueDate, personalisation.get("appealSubmittedDaysAfter"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
    }
}
