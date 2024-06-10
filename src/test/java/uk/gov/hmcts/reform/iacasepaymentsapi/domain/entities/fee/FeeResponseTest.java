package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FeeResponseTest {

    private static final BigDecimal amount = new BigDecimal("140.00");
    private static final String description = "Appeal determined with a hearing";
    private static final String version = "2";
    private static final String code = "FEE0123";

    private FeeResponse feeResponse;

    @BeforeEach
    public void setUp() {

        feeResponse = new FeeResponse(code, description, version, amount);
    }

    @Test
    void should_hold_onto_values() {

        Assertions.assertEquals(code, feeResponse.getCode());
        Assertions.assertEquals(description, feeResponse.getDescription());
        Assertions.assertEquals(version, feeResponse.getVersion());
        Assertions.assertEquals(amount, feeResponse.getAmount());
    }

    @Test
    void should_throw_required_field_exception() {

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
    void should_test_to_string_and_hash_code() {

        FeeResponse expectedFeeResponse = new FeeResponse(code, description, version, amount);
        Assertions.assertEquals(feeResponse, expectedFeeResponse);
        Assertions.assertEquals(feeResponse.hashCode(), expectedFeeResponse.hashCode());
    }

}
