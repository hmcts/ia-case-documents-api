package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CONTACT_PREFERENCE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.EMAIL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.MOBILE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SUBSCRIPTIONS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType.SMS;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
public class RecipientFinderTest {

    private final RecipientFinder recipientsFinder = new RecipientFinder();
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

        Set<String> result = recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);

        assertNotNull(result);
        assertTrue(result.contains(subscriber.getEmail()));
    }

    @Test
    public void should_return_empty_set_if_emails_not_found() {

        when(asylumCase.read(SUBSCRIPTIONS)).thenReturn(Optional.of(Collections.emptyList()));

        Set<String> result = recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);

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

        assertThatThrownBy(() -> recipientsFinder.findAll(null, NotificationType.EMAIL))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");

        assertThatThrownBy(() -> recipientsFinder.findAll(null, SMS))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");

        assertThatThrownBy(() -> recipientsFinder.findReppedAppellant(null, NotificationType.EMAIL))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");

        assertThatThrownBy(() -> recipientsFinder.findReppedAppellant(null, SMS))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @ParameterizedTest
    @EnumSource(value = ContactPreference.class)
    public void should_return_empty_set_if_field_not_found_repped(ContactPreference contactPreference) {

        when(asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class))
            .thenReturn(Optional.of(contactPreference));

        Set<String> resultSms = recipientsFinder.findReppedAppellant(asylumCase, SMS);
        Set<String> resultEmail = recipientsFinder.findReppedAppellant(asylumCase, NotificationType.EMAIL);

        assertNotNull(resultSms);
        assertEquals(0, resultSms.size());
        assertNotNull(resultEmail);
        assertEquals(0, resultEmail.size());
    }

    @Test
    public void should_return_empty_set_if_no_contact_preference() {
        Set<String> resultSms = recipientsFinder.findReppedAppellant(asylumCase, SMS);
        Set<String> resultEmail = recipientsFinder.findReppedAppellant(asylumCase, NotificationType.EMAIL);

        assertNotNull(resultSms);
        assertEquals(0, resultSms.size());
        assertNotNull(resultEmail);
        assertEquals(0, resultEmail.size());
    }

    @Test
    public void should_return_email_set_repped() {
        when(asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class))
            .thenReturn(Optional.of(ContactPreference.WANTS_EMAIL));
        when(asylumCase.read(EMAIL, String.class)).thenReturn(Optional.of("1234@test.com"));

        Set<String> resultEmail = recipientsFinder.findReppedAppellant(asylumCase, NotificationType.EMAIL);

        assertNotNull(resultEmail);
        assertEquals(1, resultEmail.size());
        assertTrue(resultEmail.contains("1234@test.com"));
    }

    @Test
    public void should_return_mobile_number_set_repped() {
        when(asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class))
            .thenReturn(Optional.of(ContactPreference.WANTS_SMS));
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.of("1234"));

        Set<String> resultSms = recipientsFinder.findReppedAppellant(asylumCase, SMS);

        assertNotNull(resultSms);
        assertEquals(1, resultSms.size());
        assertTrue(resultSms.contains("1234"));
    }
}