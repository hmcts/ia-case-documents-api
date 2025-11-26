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
                PRIOR_APPLICATIONS, UNSIGNED_DECISION_DOCUMENTS_WITH_METADATA, SIGNED_DECISION_DOCUMENT_WITH_METADATA, TTL);

    @Test
    public void mapped_to_equivalent_field_name() {
        Stream.of(BailCaseFieldDefinition.values())
            .filter(v -> !fieldsNamesWithDifferentNaming.contains(v))
            .forEach(v -> assertEquals(UPPER_UNDERSCORE.to(LOWER_CAMEL, v.name()), v.value()));

        assertEquals("supporterDOB", SUPPORTER_DOB.value());
        assertEquals("supporter2DOB", SUPPORTER_2_DOB.value());
        assertEquals("supporter3DOB", SUPPORTER_3_DOB.value());
        assertEquals("supporter4DOB", SUPPORTER_4_DOB.value());
        assertEquals("applicantArrivalInUKDate", APPLICANT_ARRIVAL_IN_UK.value());
        assertEquals("priorApplications1", PRIOR_APPLICATIONS.value());
        assertEquals("unsgnDecisionDocumentWithMetadata", UNSIGNED_DECISION_DOCUMENTS_WITH_METADATA.value());
        assertEquals("signDecisionDocumentWithMetadata", SIGNED_DECISION_DOCUMENT_WITH_METADATA.value());
        assertEquals("TTL", TTL.value());
    }

    @Test
    public void should_fail_if_new_fields_added_in_class() {
        assertEquals(204, values().length);
    }
}
