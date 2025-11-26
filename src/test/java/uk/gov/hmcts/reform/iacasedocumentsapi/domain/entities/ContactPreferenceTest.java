package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class ContactPreferenceTest {

    @Test
    void has_correct_asylum_contact_preference() {
        assertEquals(ContactPreference.from("wantsEmail").get(), ContactPreference.WANTS_EMAIL);
        assertEquals(ContactPreference.from("wantsSms").get(), ContactPreference.WANTS_SMS);
    }

    @Test
    void has_correct_asylum_string_values_for_contact_preference() {
        assertEquals("wantsEmail: Email", ContactPreference.WANTS_EMAIL.toString());
        assertEquals("wantsSms: Text message", ContactPreference.WANTS_SMS.toString());
    }

    @Test
    void has_correct_asylum_values_for_contact_preference() {
        assertEquals("wantsEmail", ContactPreference.WANTS_EMAIL.getValue());
        assertEquals("wantsSms", ContactPreference.WANTS_SMS.getValue());
    }

    @Test
    void has_correct_asylum_contact_preference_description() {
        assertEquals("Email", ContactPreference.WANTS_EMAIL.getDescription());
        assertEquals("Text message", ContactPreference.WANTS_SMS.getDescription());
    }

    @Test
    void returns_optional_for_unknown_contact_preference() {
        assertEquals(ContactPreference.from("unknown_contact_type"), Optional.empty());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, ContactPreference.values().length);
    }
}
