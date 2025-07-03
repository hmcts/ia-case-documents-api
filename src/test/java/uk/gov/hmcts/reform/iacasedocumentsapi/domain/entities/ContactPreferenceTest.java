package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class ContactPreferenceTest {

    @Test
    public void has_correct_asylum_contact_preference() {
        assertEquals(ContactPreference.from("wantsEmail").get(), ContactPreference.WANTS_EMAIL);
        assertEquals(ContactPreference.from("wantsSms").get(), ContactPreference.WANTS_SMS);
    }

    @Test
    public void has_correct_asylum_string_values_for_contact_preference() {
        assertEquals("wantsEmail", ContactPreference.WANTS_EMAIL.toString());
        assertEquals("wantsSms", ContactPreference.WANTS_SMS.toString());
    }

    @Test
    public void returns_optional_for_unknown_contact_preference() {
        assertEquals(ContactPreference.from("unknown_contact_type"), Optional.empty());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, ContactPreference.values().length);
    }
}
