package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.TTL;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class AsylumCaseDefinitionTest {
    List<AsylumCaseDefinition> fieldsNamesWithDifferentNaming =
            Collections.singletonList(TTL);

    @Test
    public void mapped_to_equivalent_field_name() {
        Stream.of(AsylumCaseDefinition.values())
            .filter(v -> !fieldsNamesWithDifferentNaming.contains(v))
            .forEach(v -> assertEquals(UPPER_UNDERSCORE.to(LOWER_CAMEL, v.name()), v.value()));

        assertEquals(TTL.value(), "TTL");
    }
}
