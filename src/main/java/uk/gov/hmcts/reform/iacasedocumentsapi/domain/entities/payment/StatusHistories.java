package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.payment;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

public class StatusHistories {

    private String status;

    @Getter
    @JsonProperty("error_code")
    private String errorCode;

    @Getter
    @JsonProperty("error_message")
    private String errorMessage;

    @Getter
    @JsonProperty("date_created")
    private String dateCreated;

    @Getter
    @JsonProperty("date_updated")
    private String dateUpdated;

    private StatusHistories() {
    }

    public StatusHistories(
        String status,
        String errorCode,
        String errorMessage,
        String dateCreated,
        String dateUpdated
    ) {
        this.status = status;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
    }

    public String getStatus() {
        requireNonNull(status);
        return status;
    }

}
