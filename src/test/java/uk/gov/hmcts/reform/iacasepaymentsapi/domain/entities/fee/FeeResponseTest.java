package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FeeResponseTest {

    private BigDecimal amount = new BigDecimal("140.00");
    private String description = "Appeal determined with a hearing";
    private String version = "2";
    private String code = "FEE0123";

    private FeeResponse feeResponse;

    @BeforeEach
    public void setUp() {

        feeResponse = new FeeResponse(code, description, version, amount);
    }

    @Test
    public void should_hold_onto_values() {

        assertEquals(feeResponse.getCode(), code);
        assertEquals(feeResponse.getDescription(), description);
        assertEquals(feeResponse.getVersion(), version);
        assertEquals(feeResponse.getAmount(), amount);
    }

    @Test
    public void should_throw_required_field_exception() {

        feeResponse = new FeeResponse(null, null, null, null);

        assertThatThrownBy(feeResponse::getCode)
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(feeResponse::getDescription)
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(feeResponse::getVersion)
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(feeResponse::getAmount)
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_test_to_string_and_hash_code() {

        FeeResponse expectedFeeResponse = new FeeResponse(code, description, version, amount);
        assertEquals(feeResponse, expectedFeeResponse);
        assertEquals(feeResponse.hashCode(), expectedFeeResponse.hashCode());
    }

}
