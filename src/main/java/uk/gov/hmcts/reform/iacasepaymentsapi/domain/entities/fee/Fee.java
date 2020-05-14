package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Fee {

    private BigDecimal calculatedAmount;
    private String description;
    private Integer version;
    private String code;

    private Fee() {

    }

    public Fee(String code, String description, Integer version, BigDecimal calculatedAmount) {

        this.calculatedAmount = calculatedAmount;
        this.description = description;
        this.version = version;
        this.code = code;
    }

    public BigDecimal getCalculatedAmount() {
        requireNonNull(calculatedAmount);
        return calculatedAmount;
    }

    public String getDescription() {
        requireNonNull(description);
        return description;
    }

    public Integer getVersion() {
        requireNonNull(version);
        return version;
    }

    public String getCode() {
        requireNonNull(code);
        return code;
    }

}
