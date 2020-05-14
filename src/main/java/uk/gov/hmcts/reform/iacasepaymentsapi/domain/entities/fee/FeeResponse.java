package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class FeeResponse {

    private String code;
    private String description;
    private Integer version;

    @JsonProperty(value = "fee_amount")
    private BigDecimal amount;

    private FeeResponse() {

    }

    public FeeResponse(String code, String description, Integer version, BigDecimal amount) {

        this.code = code;
        this.description = description;
        this.version = version;
        this.amount = amount;
    }

    public String getCode() {
        requireNonNull(code);
        return code;
    }

    public String getDescription() {
        requireNonNull(description);
        return description;
    }

    public Integer getVersion() {
        requireNonNull(version);
        return version;
    }

    public BigDecimal getAmount() {
        requireNonNull(amount);
        return amount;
    }
}
