package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

public class BailCaseFieldDefinitionTest {

    List<BailCaseFieldDefinition> fieldsNamesWithDifferentNaming =
        Arrays.asList(SUPPORTER_DOB, SUPPORTER_2_DOB, SUPPORTER_3_DOB, SUPPORTER_4_DOB, APPLICANT_ARRIVAL_IN_UK,
                PRIOR_APPLICATIONS, UNSIGNED_DECISION_DOCUMENTS_WITH_METADATA, SIGNED_DECISION_DOCUMENT_WITH_METADATA);

    @Test
    public void mapped_to_equivalent_field_name() {
        Stream.of(BailCaseFieldDefinition.values())
            .filter(v -> !fieldsNamesWithDifferentNaming.contains(v))
            .forEach(v -> assertEquals(UPPER_UNDERSCORE.to(LOWER_CAMEL, v.name()), v.value()));

        assertEquals(SUPPORTER_DOB.value(), "supporterDOB");
        assertEquals(SUPPORTER_2_DOB.value(), "supporter2DOB");
        assertEquals(SUPPORTER_3_DOB.value(), "supporter3DOB");
        assertEquals(SUPPORTER_4_DOB.value(), "supporter4DOB");
        assertEquals(APPLICANT_ARRIVAL_IN_UK.value(), "applicantArrivalInUKDate");
        assertEquals(SIGNED_DECISION_DOCUMENT_WITH_METADATA.value(), "signDecisionDocumentWithMetadata");
    }

    @Test
    public void should_fail_if_new_fields_added_in_class() {
        assertEquals(170, values().length);
    }
}
