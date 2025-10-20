package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.payment;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.fee.Fee;

@Builder
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreditAccountPayment {

    private String accountNumber;
    private BigDecimal amount;
    @Getter
    private String caseReference;
    @Getter
    private String ccdCaseNumber;
    private Currency currency;
    private String customerReference;
    private String description;
    @Setter
    @Getter
    private String organisationName;
    @Setter
    @Getter
    private Service service;
    @Setter
    @Getter
    private String siteId;
    private List<Fee> fees;

    private CreditAccountPayment() {
    }

    public CreditAccountPayment(String accountNumber,
                                BigDecimal amount,
                                String caseReference,
                                String ccdCaseNumber,
                                Currency currency,
                                String customerReference,
                                String description,
                                String organisationName,
                                Service service,
                                String siteId,
                                List<Fee> fees) {
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.caseReference = caseReference;
        this.ccdCaseNumber = ccdCaseNumber;
        this.currency = currency;
        this.customerReference = customerReference;
        this.description = description;
        this.organisationName = organisationName;
        this.service = service;
        this.siteId = siteId;
        this.fees = fees;
    }

    public String getAccountNumber() {
        requireNonNull(accountNumber);
        return accountNumber;
    }

    public BigDecimal getAmount() {
        requireNonNull(amount);
        return amount;
    }

    public Currency getCurrency() {
        requireNonNull(currency);
        return currency;
    }

    public String getCustomerReference() {
        requireNonNull(customerReference);
        return customerReference;
    }

    public String getDescription() {
        requireNonNull(description);
        return description;
    }

    public List<Fee> getFees() {
        requireNonNull(fees);
        return fees;
    }

}
