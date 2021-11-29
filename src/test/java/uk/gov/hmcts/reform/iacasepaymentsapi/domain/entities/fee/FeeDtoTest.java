package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class FeeDtoTest {

    private final String code = "FEE0001";
    private final String version = "1";
    private final Integer volume = 1;
    private final BigDecimal calculatedAmount = new BigDecimal("140");
    private final String memoLine = "memoLine";
    private final String ccdCaseNumber = "1234";
    private final String reference = "RC-1627-5070-9329-7815";

    private FeeDto feeDto;

    @Test
    void should_test_equals_contract() {

        EqualsVerifier.simple()
            .forClass(FeeDto.class)
            .verify();
    }

    @Test
    void should_hold_onto_values() {

        feeDto = FeeDto.builder()
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
