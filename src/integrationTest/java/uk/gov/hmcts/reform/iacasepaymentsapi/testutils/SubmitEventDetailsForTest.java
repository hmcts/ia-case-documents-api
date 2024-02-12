package uk.gov.hmcts.reform.iacasepaymentsapi.testutils;

import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.SubmitEventDetails;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers.ServiceRequestUpdateIntegrationTest.SERVICE_REQUEST_REFERENCE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient.CALLBACK_COMPLETED;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient.CCD_CASE_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient.ID;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient.JURISDICTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient.PAYMENT_AMOUNT;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient.PAYMENT_CASE_REFERENCE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient.SUCCESS;

public class SubmitEventDetailsForTest extends SubmitEventDetails {

    public static SubmitEventDetails generateValidPaymentUpdateEvent() {
        return new SubmitEventDetails(
            Long.parseLong(ID),
            JURISDICTION,
            State.APPEAL_SUBMITTED,
            generateEventDataForUpdatePaymentStatus(),
            200,
            CALLBACK_COMPLETED
        );
    }

    public static SubmitEventDetails generateValidServiceRequestEvent() {
        return new SubmitEventDetails(
            Long.parseLong(ID),
            JURISDICTION,
            State.APPEAL_SUBMITTED,
            generateEventDataForServiceRequestUpdate(),
            200,
            CALLBACK_COMPLETED
        );
    }

    private static Map<String, Object> generateEventDataForUpdatePaymentStatus() {
        Map<String, Object> data = new HashMap<>();
        data.put(APPEAL_REFERENCE_NUMBER.value(), IaCasePaymentApiClient.APPEAL_REFERENCE_NUMBER);
        data.put(PAYMENT_STATUS.value(), SUCCESS);
        data.put(PAYMENT_REFERENCE.value(), CCD_CASE_NUMBER);
        return data;
    }

    private static Map<String, Object> generateEventDataForServiceRequestUpdate() {
        Map<String, Object> payment = new HashMap<>();
        payment.put("payment_reference", PAYMENT_CASE_REFERENCE);
        payment.put("status", SUCCESS);

        Map<String, Object> data = new HashMap<>();
        data.put("ccd_case_number", CCD_CASE_NUMBER);
        data.put("service_request_reference", SERVICE_REQUEST_REFERENCE);
        data.put("service_request_status", "paid");
        data.put("service_request_amount", PAYMENT_AMOUNT);
        data.put("payment", payment);
        return data;
    }

}
