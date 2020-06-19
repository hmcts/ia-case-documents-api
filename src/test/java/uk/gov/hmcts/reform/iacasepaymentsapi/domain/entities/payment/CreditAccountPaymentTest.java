package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;

class CreditAccountPaymentTest {

    private String accountNumber = "PBA0072626";
    private BigDecimal amount = new BigDecimal("140.00");
    private String caseReference = "caseReference";
    private String ccdCaseNumber = "ccdCaseNumber";
    private Currency currency = Currency.GBP;
    private String customerReference = "customerReference";
    private String description = "Some description";
    private String organisationName = "immigration & asylum chamber";
    private Service service = Service.IAC;
    private String siteId = "AA001";
    private List<Fee> fees = Arrays.asList(new Fee("FEE0123",
        "Fee description", "1", new BigDecimal("140.00")));

    private CreditAccountPayment creditAccountPayment;

    @BeforeEach
    public void setUp() {

        creditAccountPayment = new CreditAccountPayment(
            accountNumber, amount, caseReference, ccdCaseNumber,
            currency, customerReference, description, fees);
        creditAccountPayment.setOrganisationName(organisationName);
        creditAccountPayment.setService(service);
        creditAccountPayment.setSiteId(siteId);
    }

    @Test
    void should_hold_onto_values() {

        assertEquals(creditAccountPayment.getAccountNumber(), accountNumber);
        assertEquals(creditAccountPayment.getAmount(), amount);
        assertEquals(creditAccountPayment.getCaseReference(), caseReference);
        assertEquals(creditAccountPayment.getCcdCaseNumber(), ccdCaseNumber);
        assertEquals(creditAccountPayment.getCurrency(), currency);
        assertEquals(creditAccountPayment.getCustomerReference(), customerReference);
        assertEquals(creditAccountPayment.getDescription(), description);
        assertEquals(creditAccountPayment.getFees(), fees);
        assertEquals(creditAccountPayment.getOrganisationName(), organisationName);
        assertEquals(creditAccountPayment.getService(), service);
        assertEquals(creditAccountPayment.getSiteId(), siteId);
    }

    @Test
    void should_throw_required_field_exception() {

        creditAccountPayment = new CreditAccountPayment(
            null, null, null, null,
            null, null, null, null);

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
