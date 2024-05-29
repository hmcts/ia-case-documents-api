package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class HasOtherAppealsTest {

    @Test
    public void has_correct_hasOtherappeals_value() {
        assertEquals(HasOtherAppeals.from("Yes").get(), HasOtherAppeals.YES);
        assertEquals(HasOtherAppeals.from("YesWithoutAppealNumber").get(), HasOtherAppeals.YES_WITHOUT_APPEAL_NUMBER);
        assertEquals(HasOtherAppeals.from("No").get(), HasOtherAppeals.NO);
        assertEquals(HasOtherAppeals.from("NotSure").get(), HasOtherAppeals.NOT_SURE);
    }

    @Test
    public void has_correct_string_values_for_hasOtherappeals() {
        assertEquals("Yes", HasOtherAppeals.YES.toString());
        assertEquals("YesWithoutAppealNumber", HasOtherAppeals.YES_WITHOUT_APPEAL_NUMBER.toString());
        assertEquals("No", HasOtherAppeals.NO.toString());
        assertEquals("NotSure", HasOtherAppeals.NOT_SURE.toString());
    }

    @Test
    public void returns_optional_for_unknown_hasOtherappeals_value() {
        assertEquals(HasOtherAppeals.from("unknown_value"), Optional.empty());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(4, HasOtherAppeals.values().length);
    }

}