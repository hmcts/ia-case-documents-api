package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.payment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.fee.Fee;

class CreditAccountPaymentTest {

    private final String accountNumber = "PBA0072626";
    private final BigDecimal amount = new BigDecimal("140.00");
    private final String caseReference = "caseReference";
    private final String ccdCaseNumber = "ccdCaseNumber";
    private final Currency currency = Currency.GBP;
    private final String customerReference = "customerReference";
    private final String description = "Some description";
    private final String organisationName = "immigration & asylum chamber";
    private final Service service = Service.IAC;
    private final String siteId = "AA001";
    private final List<Fee> fees = List.of(new Fee(
        "FEE0123",
        "Fee description", "1", new BigDecimal("140.00")
    ));

    private CreditAccountPayment creditAccountPayment;

    @BeforeEach
    public void setUp() {
        creditAccountPayment = new CreditAccountPayment(
            accountNumber, amount, caseReference, ccdCaseNumber,
            currency, customerReference, description, "ia-legal-rep-org",
            Service.IAC,
            "BFA1", fees);
        creditAccountPayment.setOrganisationName(organisationName);
        creditAccountPayment.setService(service);
        creditAccountPayment.setSiteId(siteId);
    }

    @Test
    void should_hold_onto_values() {

        Assertions.assertEquals(creditAccountPayment.getAccountNumber(), accountNumber);
        Assertions.assertEquals(creditAccountPayment.getAmount(), amount);
        Assertions.assertEquals(creditAccountPayment.getCaseReference(), caseReference);
        Assertions.assertEquals(creditAccountPayment.getCcdCaseNumber(), ccdCaseNumber);
        Assertions.assertEquals(creditAccountPayment.getCurrency(), currency);
        Assertions.assertEquals(creditAccountPayment.getCustomerReference(), customerReference);
        Assertions.assertEquals(creditAccountPayment.getDescription(), description);
        Assertions.assertEquals(creditAccountPayment.getFees(), fees);
        Assertions.assertEquals(creditAccountPayment.getOrganisationName(), organisationName);
        Assertions.assertEquals(creditAccountPayment.getService(), service);
        Assertions.assertEquals(creditAccountPayment.getSiteId(), siteId);
    }

    @Test
    void should_throw_required_field_exception() {

        creditAccountPayment = new CreditAccountPayment(
            null, null, null, null,
            null, null, null, null, null, null, null);

        assertThatThrownBy(creditAccountPayment::getAccountNumber)
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(creditAccountPayment::getAmount)
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(creditAccountPayment::getCurrency)
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(creditAccountPayment::getCustomerReference)
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(creditAccountPayment::getDescription)
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(creditAccountPayment::getFees)
            .isExactlyInstanceOf(NullPointerException.class);

    }

}
