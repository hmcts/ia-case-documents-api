package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;

public class BailCaseDefinitionTest {

    List<uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition> fieldsNamesWithDifferentNaming =
        Arrays.asList(SUPPORTER_DOB, SUPPORTER_2_DOB, SUPPORTER_3_DOB, SUPPORTER_4_DOB, TTL);

    @Test
    public void mapped_to_equivalent_field_name() {
        Stream.of(BailCaseFieldDefinition.values())
            .filter(v -> !fieldsNamesWithDifferentNaming.contains(v))
            .forEach(v -> assertEquals(UPPER_UNDERSCORE.to(LOWER_CAMEL, v.name()), v.value()));

        assertEquals(SUPPORTER_DOB.value(), "supporterDOB");
        assertEquals(SUPPORTER_2_DOB.value(), "supporter2DOB");
        assertEquals(SUPPORTER_3_DOB.value(), "supporter3DOB");
        assertEquals(SUPPORTER_4_DOB.value(), "supporter4DOB");
        assertEquals(TTL.value(), "TTL");
    }

    @Test
    public void should_fail_if_new_fields_added_in_class() {
        assertEquals(151, values().length);
    }
}
