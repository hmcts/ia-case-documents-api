package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FeeTest {

    private static final BigDecimal calculatedAmount = new BigDecimal("140.00");
    private static final String description = "Appeal determined with a hearing";
    private static final String version = "2";
    private static final String code = "FEE0123";

    private static Fee fee;

    @BeforeEach
    public void setUp() {
        fee = new Fee(code, description, version, calculatedAmount);
    }

    @Test
    void should_hold_onto_values() {
        Assertions.assertEquals(code, fee.getCode());
        Assertions.assertEquals(description, fee.getDescription());
        Assertions.assertEquals(version, fee.getVersion());
        Assertions.assertEquals(calculatedAmount, fee.getCalculatedAmount());
    }

    @Test
    void should_throw_required_field_exception() {
        fee = new Fee(null, null, null, null);

        assertThatThrownBy(fee::getCode)
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(fee::getDescription)
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(fee::getVersion)
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(fee::getCalculatedAmount)
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_format_fee_for_display() {
        Assertions.assertEquals("100", new Fee("ABC", "desc", "1", new BigDecimal("100.00")).getAmountAsString());
        Assertions.assertEquals("100.5", new Fee("ABC", "desc", "1", new BigDecimal("100.50")).getAmountAsString());
        Assertions.assertEquals("100.05", new Fee("ABC", "desc", "1", new BigDecimal("100.05")).getAmountAsString());
        Assertions.assertEquals("100.55", new Fee("ABC", "desc", "1", new BigDecimal("100.55")).getAmountAsString());
        Assertions.assertEquals("100.5", new Fee("ABC", "desc", "1", new BigDecimal("100.5")).getAmountAsString());
        Assertions.assertEquals("100", new Fee("ABC", "desc", "1", new BigDecimal("100")).getAmountAsString());
    }
}
