package uk.gov.hmcts.reform.iacasedocumentsapi.utilities.fixtures;

import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.payment.ServiceRequestUpdateDto;

import static uk.gov.hmcts.reform.iacasedocumentsapi.utilities.IaCasePaymentApiClient.CCD_CASE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.utilities.IaCasePaymentApiClient.PAYMENT_AMOUNT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.utilities.IaCasePaymentApiClient.PAYMENT_CASE_REFERENCE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.utilities.IaCasePaymentApiClient.SUCCESS;

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
