package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd.Event;

class CaseMetaDataTest {

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

        Event event = Event.UPDATE_PAYMENT_STATUS;
        String paymentReference = "RC-1234";
        String paymentStatus = "Success";
        long caseId = 1234;
        String caseType = "Asylum";
        String jurisdiction = "IA";
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
