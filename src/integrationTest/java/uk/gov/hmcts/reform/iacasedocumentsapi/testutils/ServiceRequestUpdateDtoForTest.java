package uk.gov.hmcts.reform.iacasedocumentsapi.testutils;

import static uk.gov.hmcts.reform.iacasedocumentsapi.testutils.IaCasePaymentApiClient.CCD_CASE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.testutils.IaCasePaymentApiClient.PAYMENT_AMOUNT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.testutils.IaCasePaymentApiClient.PAYMENT_CASE_REFERENCE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.testutils.IaCasePaymentApiClient.SUCCESS;

import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.payment.ServiceRequestUpdateDto;

public class ServiceRequestUpdateDtoForTest extends ServiceRequestUpdateDto {

    public static ServiceRequestUpdateDtoBuilder generateValid() {
        return builder()
            .serviceRequestReference(PAYMENT_CASE_REFERENCE)
            .ccdCaseNumber(CCD_CASE_NUMBER)
            .serviceRequestAmount(PAYMENT_AMOUNT.toString())
            .serviceRequestStatus(SUCCESS)
            .payment(PaymentDtoForTest.generateValid().build());
    }

}
