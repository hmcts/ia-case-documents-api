package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.PRISON_NOMS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.TTL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

public class AsylumCaseDefinitionTest {
    List<AsylumCaseDefinition> fieldsNamesWithDifferentNaming =
        new ArrayList<>(Arrays.asList(TTL, PRISON_NOMS));

    @Test
    public void mapped_to_equivalent_field_name() {
        Stream.of(AsylumCaseDefinition.values())
            .filter(v -> !fieldsNamesWithDifferentNaming.contains(v))
            .forEach(v -> assertEquals(UPPER_UNDERSCORE.to(LOWER_CAMEL, v.name()), v.value()));

        assertEquals("TTL", TTL.value());
        assertEquals("prisonNOMSNumber", PRISON_NOMS.value());
    }
}
