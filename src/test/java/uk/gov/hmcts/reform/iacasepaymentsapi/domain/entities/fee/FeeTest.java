package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FeeTest {

    private BigDecimal calculatedAmount = new BigDecimal("140.00");
    private String description = "Appeal determined with a hearing";
    private String version = "2";
    private String code = "FEE0123";

    private Fee fee;

    @BeforeEach
    public void setUp() {

        fee = new Fee(code, description, version, calculatedAmount);
    }

    @Test
    public void should_hold_onto_values() {

        assertEquals(fee.getCode(), code);
        assertEquals(fee.getDescription(), description);
        assertEquals(fee.getVersion(), version);
        assertEquals(fee.getCalculatedAmount(), calculatedAmount);
    }

    @Test
    public void should_throw_required_field_exception() {

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

        Assertions.assertEquals("£100", new Fee("ABC", "desc", "1", new BigDecimal(100.00)).getFeeForDisplay());
        Assertions.assertEquals("£100.50", new Fee("ABC", "desc", "1", new BigDecimal(100.50)).getFeeForDisplay());
        Assertions.assertEquals("£100.05", new Fee("ABC", "desc", "1", new BigDecimal(100.05)).getFeeForDisplay());
        Assertions.assertEquals("£100.55", new Fee("ABC", "desc", "1", new BigDecimal(100.55)).getFeeForDisplay());
        Assertions.assertEquals("£100.50", new Fee("ABC", "desc", "1", new BigDecimal(100.5)).getFeeForDisplay());
        Assertions.assertEquals("£100", new Fee("ABC", "desc", "1", new BigDecimal(100)).getFeeForDisplay());
    }
}
