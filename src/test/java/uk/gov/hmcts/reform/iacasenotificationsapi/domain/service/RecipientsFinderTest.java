package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SUBSCRIPTIONS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType.EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType.SMS;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Subscriber;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.SubscriberType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
public class RecipientsFinderTest {

    private final RecipientsFinder recipientsFinder = new RecipientsFinder();
    @Mock
    private AsylumCase asylumCase;

    private String mockedAppellantEmailAddress = "appelant@example.net";
    private String mockedAppellantMobilePhone = "07123456789";

    @Test
    public void should_find_all_email_recipients() {

        Subscriber subscriber = new Subscriber(
            SubscriberType.APPELLANT, //subscriberType
            mockedAppellantEmailAddress, //email
            YesOrNo.YES, // wants email
            "", //mobileNumber
            YesOrNo.NO // wants sms
        );

        when(asylumCase.read(SUBSCRIPTIONS))
            .thenReturn(Optional.of(Collections.singletonList(new IdValue<>("foo", subscriber))));

        Set<String> result = recipientsFinder.findAll(asylumCase, EMAIL);

        assertNotNull(result);
        assertTrue(result.contains(subscriber.getEmail()));
    }

    @Test
    public void should_return_empty_set_if_emails_not_found() {

        when(asylumCase.read(SUBSCRIPTIONS)).thenReturn(Optional.of(Collections.emptyList()));

        Set<String> result = recipientsFinder.findAll(asylumCase, EMAIL);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void should_find_all_mobile_phone_recipients() {

        Subscriber subscriber = new Subscriber(
            SubscriberType.APPELLANT, //subscriberType
            "", //email
            YesOrNo.NO, // wants email
            mockedAppellantMobilePhone, //mobileNumber
            YesOrNo.YES // wants sms
        );

        when(asylumCase.read(SUBSCRIPTIONS))
            .thenReturn(Optional.of(Collections.singletonList(new IdValue<>("foo", subscriber))));

        Set<String> result = recipientsFinder.findAll(asylumCase, SMS);

        assertNotNull(result);
        assertTrue(result.contains(subscriber.getMobileNumber()));
    }

    @Test
    public void should_return_empty_set_if_mobile_phone_not_found() {

        when(asylumCase.read(SUBSCRIPTIONS)).thenReturn(Optional.of(Collections.emptyList()));

        Set<String> result = recipientsFinder.findAll(asylumCase, SMS);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> recipientsFinder.findAll(null, EMAIL))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");

        assertThatThrownBy(() -> recipientsFinder.findAll(null, SMS))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

}
