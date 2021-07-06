package uk.gov.hmcts.reform.iacasedocumentsapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS;

import java.util.Optional;
import org.junit.jupiter.api.Test;


class OutOfCountryDecisionTypeTest {

    @Test
    void has_correct_asylum_out_of_country_decision_types() {
        assertEquals(OutOfCountryDecisionType.from("refusalOfHumanRights"), Optional.of(REFUSAL_OF_HUMAN_RIGHTS));
        assertEquals(OutOfCountryDecisionType.from("refusalOfProtection"),Optional.of(OutOfCountryDecisionType.REFUSAL_OF_PROTECTION));
        assertEquals(OutOfCountryDecisionType.from("removalOfClient"),Optional.of(OutOfCountryDecisionType.REMOVAL_OF_CLIENT));

    }

    @Test
    void has_correct_asylum_out_of_country_decision_types_description() {
        assertEquals("A decision either 1) to refuse a human rights claim made following an application for entry clearance or 2) to refuse a permit to enter the UK under the Immigration (European Economic Area) Regulation 2016", REFUSAL_OF_HUMAN_RIGHTS.getDescription());
        assertEquals("A decision to refuse a protection or human rights claim where your client may only apply after leaving the UK", OutOfCountryDecisionType.REFUSAL_OF_PROTECTION.getDescription());
        assertEquals("A decision either 1) to remove your client from the UK under the Immigration (European Economic Area) Regulations 2016, where they are currently outside the UK or 2) to deprive your client of British citizenship, where they are currently outside the UK", OutOfCountryDecisionType.REMOVAL_OF_CLIENT.getDescription());
    }

    @Test
    void returns_optional_for_unknown_out_of_country_decision_type() {
        assertThat(OutOfCountryDecisionType.from("some_unknown_type")).isEmpty();
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(3, OutOfCountryDecisionType.values().length);
    }

    @Test
    void has_correct_value_and_description() {
        assertEquals("refusalOfHumanRights: A decision either 1) to refuse a human rights claim made following an application for entry clearance or 2) to refuse a permit to enter the UK under the Immigration (European Economic Area) Regulation 2016",
                OutOfCountryDecisionType.from("refusalOfHumanRights").get().toString());
    }
}
