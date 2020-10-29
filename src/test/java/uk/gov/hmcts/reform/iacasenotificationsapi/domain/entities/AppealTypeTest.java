package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;

public class AppealTypeTest {

    @Test
    public void has_correct_asylum_appeal_types() {
        assertEquals(AppealType.from("revocationOfProtection").get(), AppealType.RP);
        assertEquals(AppealType.from("protection").get(), AppealType.PA);
        assertEquals(AppealType.from("refusalOfEu").get(), AppealType.EA);
        assertEquals(AppealType.from("refusalOfHumanRights").get(), AppealType.HU);
        assertEquals(AppealType.from("deprivation").get(), AppealType.DC);
    }

    @Test
    public void returns_optional_for_unknown_appeal_type() {
        assertEquals(AppealType.from("some_unknown_type"), Optional.empty());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(5, AppealType.values().length);
    }
}
