package uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures;

import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.IaCaseDocumentsApiClient.CCD_CASE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.IaCaseDocumentsApiClient.PAYMENT_AMOUNT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.IaCaseDocumentsApiClient.PAYMENT_CASE_REFERENCE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.IaCaseDocumentsApiClient.SUCCESS;

import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.PaymentDtoForTest;

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
