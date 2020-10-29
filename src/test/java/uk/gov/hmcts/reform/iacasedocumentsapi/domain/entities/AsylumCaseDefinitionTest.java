package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

public class AsylumCaseDefinitionTest {

    @Test
    public void mapped_to_equivalent_field_name() {
        Stream.of(AsylumCaseDefinition.values())
                .forEach(v -> assertEquals(UPPER_UNDERSCORE.to(LOWER_CAMEL, v.name()), v.value()));
    }
}
