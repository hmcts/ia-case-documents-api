package uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures;

import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.IaCaseDocumentsApiClient.CCD_CASE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.IaCaseDocumentsApiClient.ID;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.IaCaseDocumentsApiClient.PAYMENT_AMOUNT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.IaCaseDocumentsApiClient.PAYMENT_CASE_REFERENCE;

import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.payment.PaymentDto;

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
