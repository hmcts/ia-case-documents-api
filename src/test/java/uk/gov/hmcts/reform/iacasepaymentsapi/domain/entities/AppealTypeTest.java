package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;

public class AppealTypeTest {

    @Test
    public void has_correct_asylum_appeal_types() {
        assertThat(AppealType.from("revocationOfProtection").get(), is(AppealType.RP));
        assertThat(AppealType.from("protection").get(), is(AppealType.PA));
        assertThat(AppealType.from("refusalOfEu").get(), is(AppealType.EA));
        assertThat(AppealType.from("refusalOfHumanRights").get(), is(AppealType.HU));
        assertThat(AppealType.from("deprivation").get(), is(AppealType.DC));
        assertThat(AppealType.from("euSettlementScheme").get(), is(AppealType.EU));
    }

    @Test
    public void returns_optional_for_unknown_appeal_type() {
        assertThat(AppealType.from("some_unknown_type"), is(Optional.empty()));
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(6, AppealType.values().length);
    }
}
