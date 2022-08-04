package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PriorApplicationTest {

    private final String applicationId = "someAppId1";
    private final String caseDataJson = "{\"exampleField\" : \"exampleData\"";

    private PriorApplication priorApplication = new PriorApplication(
        applicationId,
        caseDataJson
    );

    @Test
    public void should_hold_onto_values() {

        assertEquals(applicationId, priorApplication.getApplicationId());
        assertEquals(caseDataJson, priorApplication.getCaseDataJson());

    }
}
