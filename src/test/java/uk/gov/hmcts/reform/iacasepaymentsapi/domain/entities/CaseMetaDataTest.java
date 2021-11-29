package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;

class CaseMetaDataTest {

    private Event event;
    private final String jurisdiction = "IA";
    private final String caseType = "Asylum";
    private final long caseId = 1234;
    private final String paymentStatus = "Success";
    private final String paymentReference = "RC-1234";

    @Test
    void should_test_equals_contract() {

        EqualsVerifier.simple()
            .forClass(CaseMetaDataTest.class)
            .suppress(
                Warning.INHERITED_DIRECTLY_FROM_OBJECT,
                Warning.ALL_FIELDS_SHOULD_BE_USED)
            .verify();
    }

    @Test
    void should_hold_onto_values() {

        event = Event.UPDATE_PAYMENT_STATUS;
        CaseMetaData caseMetaData =
            new CaseMetaData(event, jurisdiction, caseType, caseId, paymentStatus, paymentReference);

        assertEquals(Event.UPDATE_PAYMENT_STATUS, caseMetaData.getEvent());
        assertEquals("IA", caseMetaData.getJurisdiction());
        assertEquals("Asylum", caseMetaData.getCaseType());
        assertEquals(1234, caseMetaData.getCaseId());
        assertEquals("Success", caseMetaData.getPaymentStatus());
        assertEquals("RC-1234", caseMetaData.getPaymentReference());
    }
}
