package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CasePaymentRequestDtoTest {

    private static final String ACTION = "some-action";
    private static final String RESPONSIBLE_PARTY = "some-responsible-party";

    @Test
    void should_hold_onto_values() {
        CasePaymentRequestDto casePaymentRequestDto = new CasePaymentRequestDto();
        casePaymentRequestDto.setAction(ACTION);
        casePaymentRequestDto.setResponsibleParty(RESPONSIBLE_PARTY);

        assertEquals(ACTION, casePaymentRequestDto.getAction());
        assertEquals(RESPONSIBLE_PARTY, casePaymentRequestDto.getResponsibleParty());
    }

}
