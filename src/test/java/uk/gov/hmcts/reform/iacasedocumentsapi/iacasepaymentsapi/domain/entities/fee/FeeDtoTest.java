package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.fee;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class FeeDtoTest {

    private final BigDecimal calculatedAmount = new BigDecimal("140");

    @Test
    void should_test_equals_contract() {

        EqualsVerifier.simple()
            .forClass(FeeDto.class)
            .verify();
    }

    @Test
    void should_hold_onto_values() {

        String reference = "RC-1627-5070-9329-7815";
        String ccdCaseNumber = "1234";
        String memoLine = "memoLine";
        Integer volume = 1;
        String version = "1";
        String code = "FEE0001";
        FeeDto feeDto = FeeDto.builder()
            .code(code)
            .version(version)
            .volume(volume)
            .calculatedAmount(calculatedAmount)
            .memoLine(memoLine)
            .ccdCaseNumber(ccdCaseNumber)
            .reference(reference)
            .build();

        assertEquals(feeDto.getCode(), code);
        assertEquals(feeDto.getVersion(), version);
        assertEquals(feeDto.getVolume(), volume);
        assertEquals(feeDto.getCalculatedAmount(), calculatedAmount);
        assertEquals(feeDto.getMemoLine(), memoLine);
        assertEquals(feeDto.getCcdCaseNumber(), ccdCaseNumber);
        assertEquals(feeDto.getReference(), reference);
    }
}
