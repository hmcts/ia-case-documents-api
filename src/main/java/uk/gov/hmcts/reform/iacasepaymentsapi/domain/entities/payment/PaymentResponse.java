package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;

public class PaymentResponse {

    private String reference;

    @JsonProperty("date_created")
    private Date dateCreated;

    private String status;

    @JsonProperty("payment_group_reference")
    private String paymentGroupReference;

    @JsonProperty("status_histories")
    private List<StatusHistories> statusHistories;

    private PaymentResponse() {
    }

    public PaymentResponse(String reference, Date dateCreated,
                           String status, String paymentGroupReference,
                           List<StatusHistories> statusHistories) {
        this.reference = reference;
        this.dateCreated = dateCreated;
        this.status = status;
        this.paymentGroupReference = paymentGroupReference;
        this.statusHistories = statusHistories;
    }

    public String getReference() {
        requireNonNull(reference);
        return reference;
    }

    public Date getDateCreated() {
        requireNonNull(dateCreated);
        return dateCreated;
    }

    public String getStatus() {
        requireNonNull(status);
        return status;
    }

    public String getPaymentGroupReference() {
        requireNonNull(paymentGroupReference);
        return paymentGroupReference;
    }

    public List<StatusHistories> getStatusHistories() {
        requireNonNull(statusHistories);
        return statusHistories;
    }
}
