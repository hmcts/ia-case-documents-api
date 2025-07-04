package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;

class SubscriberTest {

    private static final SubscriberType SUBSCRIBER_TYPE = SubscriberType.APPELLANT;
    private static final String EMAIL = "test@example.com";
    private static final YesOrNo WANTS_EMAIL = YesOrNo.YES;
    private static final String MOBILE_NUMBER = "07123456789";
    private static final YesOrNo WANTS_SMS = YesOrNo.YES;

    @Test
    void should_create_subscriber_with_no_args_constructor() {
        Subscriber subscriber = new Subscriber();

        assertNotNull(subscriber);
    }

    @Test
    void should_create_subscriber_with_all_fields() {
        Subscriber subscriber = new Subscriber(
            SUBSCRIBER_TYPE,
            EMAIL,
            WANTS_EMAIL,
            MOBILE_NUMBER,
            WANTS_SMS
        );

        assertNotNull(subscriber);
        assertEquals(SUBSCRIBER_TYPE, subscriber.getSubscriber());
        assertEquals(EMAIL, subscriber.getEmail());
        assertEquals(WANTS_EMAIL, subscriber.getWantsEmail());
        assertEquals(MOBILE_NUMBER, subscriber.getMobileNumber());
        assertEquals(WANTS_SMS, subscriber.getWantsSms());
    }

    @Test
    void should_throw_null_pointer_when_subscriber_type_is_null() {
        assertThrows(
            NullPointerException.class,
            () -> new Subscriber(null, EMAIL, WANTS_EMAIL, MOBILE_NUMBER, WANTS_SMS)
        );
    }

    @Test
    void should_handle_null_optional_fields() {
        Subscriber subscriber = new Subscriber(
            SUBSCRIBER_TYPE,
            null,
            null,
            null,
            null
        );

        assertNotNull(subscriber);
        assertEquals(SUBSCRIBER_TYPE, subscriber.getSubscriber());
        assertNull(subscriber.getEmail());
        assertNull(subscriber.getWantsEmail());
        assertNull(subscriber.getMobileNumber());
        assertNull(subscriber.getWantsSms());
    }

    @Test
    void should_throw_null_pointer_when_getting_null_subscriber() {
        Subscriber subscriber = new Subscriber();
        assertThrows(
            NullPointerException.class,
            subscriber::getSubscriber
        );
    }

    @Test
    void should_handle_all_field_values_correctly() {
        Subscriber subscriber = new Subscriber(
            SubscriberType.SUPPORTER,
            "supporter@example.com",
            YesOrNo.NO,
            "07987654321",
            YesOrNo.NO
        );

        assertEquals(SubscriberType.SUPPORTER, subscriber.getSubscriber());
        assertEquals("supporter@example.com", subscriber.getEmail());
        assertEquals(YesOrNo.NO, subscriber.getWantsEmail());
        assertEquals("07987654321", subscriber.getMobileNumber());
        assertEquals(YesOrNo.NO, subscriber.getWantsSms());
    }
}
