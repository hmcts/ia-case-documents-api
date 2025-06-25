package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;

public class AsylumAppealTypeTest {

    @Test
    public void has_correct_asylum_appeal_types() {
        assertEquals(AsylumAppealType.from("revocationOfProtection").get(), AsylumAppealType.RP);
        assertEquals(AsylumAppealType.from("protection").get(), AsylumAppealType.PA);
        assertEquals(AsylumAppealType.from("refusalOfHumanRights").get(), AsylumAppealType.HU);
        assertEquals(AsylumAppealType.from("refusalOfEu").get(), AsylumAppealType.EA);
        assertEquals(AsylumAppealType.from("deprivation").get(), AsylumAppealType.DC);
        assertEquals(AsylumAppealType.from("euSettlementScheme").get(), AsylumAppealType.EU);
    }

    @Test
    public void has_correct_string_values_for_asylum_appeal_types() {
        assertEquals("revocationOfProtection: Revocation of a protection status", AsylumAppealType.RP.toString());
        assertEquals("protection: Refusal of protection claim", AsylumAppealType.PA.toString());
        assertEquals("refusalOfHumanRights: Refusal of a human rights claim", AsylumAppealType.HU.toString());
        assertEquals("refusalOfEu: Refusal of application under the EEA regulations", AsylumAppealType.EA.toString());
        assertEquals("deprivation: Deprivation of citizenship", AsylumAppealType.DC.toString());
        assertEquals("euSettlementScheme: EU Settlement Scheme", AsylumAppealType.EU.toString());
    }

    @Test
    public void returns_optional_for_unknown_appeal_type() {
        assertEquals(AsylumAppealType.from("some_unknown_type"), Optional.empty());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(6, AsylumAppealType.values().length);
    }
}
