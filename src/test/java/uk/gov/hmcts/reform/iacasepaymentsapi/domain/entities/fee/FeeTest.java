package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FeeTest {

    private BigDecimal calculatedAmount = new BigDecimal("140.00");
    private String description = "Appeal determined with a hearing";
    private String version = "1";
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
}
