package uk.gov.hmcts.reform.iacasepaymentsapi.testutils;

import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentDto;

import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient.CCD_CASE_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient.ID;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient.PAYMENT_AMOUNT;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient.PAYMENT_CASE_REFERENCE;

@SuperBuilder
public class PaymentDtoForTest extends PaymentDto {

    public static PaymentDtoBuilder generateValid() {
        return builder()
            .id(ID)
            .amount(PAYMENT_AMOUNT)
            .description("Payment status update")
            .reference(PAYMENT_CASE_REFERENCE)
            .currency("GBP")
            .ccdCaseNumber(CCD_CASE_NUMBER)
            .channel("online")
            .method("card")
            .externalProvider("gov pay")
            .externalReference("8saf7t8kav53mmubrff738nord")
            .status("Success");
    }

}
