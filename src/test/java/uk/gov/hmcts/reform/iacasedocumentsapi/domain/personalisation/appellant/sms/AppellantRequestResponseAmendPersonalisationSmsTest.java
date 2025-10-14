package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SUBSCRIPTIONS;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms.AppellantRequestResponseAmendPersonalisationSms;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Subscriber;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.SubscriberType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;



@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantRequestResponseAmendPersonalisationSmsTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;

    private Long caseId = 12345L;
    private String emailTemplateId = "someEmailTemplateId";

    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantMobilePhone = "07123456789";

    private AppellantRequestResponseAmendPersonalisationSms appellantRequestResponseAmendPersonalisationSms;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(mockedAppealReferenceNumber));

        appellantRequestResponseAmendPersonalisationSms = new AppellantRequestResponseAmendPersonalisationSms(
                emailTemplateId,
                recipientsFinder);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(emailTemplateId, appellantRequestResponseAmendPersonalisationSms.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_REQUEST_RESPONSE_AMEND_AIP_SMS",
                appellantRequestResponseAmendPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_list_from_subscribers_in_asylum_case() {

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

        assertTrue(appellantRequestResponseAmendPersonalisationSms.getRecipientsList(asylumCase)
                .contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS)).thenCallRealMethod();

        assertThatThrownBy(() -> appellantRequestResponseAmendPersonalisationSms.getRecipientsList(null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }


    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
                appellantRequestResponseAmendPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("Appeal Ref Number"));
    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
                appellantRequestResponseAmendPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("Appeal Ref Number"));
    }
}
