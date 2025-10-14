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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms.AppellantFtpaSubmittedPersonalisationSms;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Subscriber;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.SubscriberType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantFtpaSubmittedPersonalisationSmsTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;

    private Long caseId = 12345L;
    private String iaAipFrontendUrl = "frontendHyperlink";
    private String referenceNumber = "someReferenceNumber";
    private String emailTemplateId = "someEmailTemplateId";
    private String mockedAppellantMobilePhone = "07123456789";

    private AppellantFtpaSubmittedPersonalisationSms appellantFtpaSubmittedPersonalisationSms;

    @BeforeEach
    public void setup() {
        appellantFtpaSubmittedPersonalisationSms = new AppellantFtpaSubmittedPersonalisationSms(
            emailTemplateId,
            iaAipFrontendUrl,
            recipientsFinder);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(emailTemplateId, appellantFtpaSubmittedPersonalisationSms.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_APPELLANT_IN_PERSON_FTPA_SUBMITTED_SMS",
            appellantFtpaSubmittedPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_phone_number_list_from_subscribers_in_asylum_case() {

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

        assertTrue(appellantFtpaSubmittedPersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS)).thenCallRealMethod();

        assertThatThrownBy(() -> appellantFtpaSubmittedPersonalisationSms.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }


    @Test
    public void should_return_personalisation() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(referenceNumber));
        Map<String, String> personalisation =
            appellantFtpaSubmittedPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(referenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(iaAipFrontendUrl, personalisation.get("linkToService"));
    }
}
