package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StatusHistories {

    private String status;

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("date_created")
    private String dateCreated;

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

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getDateUpdated() {
        return dateUpdated;
    }
}
