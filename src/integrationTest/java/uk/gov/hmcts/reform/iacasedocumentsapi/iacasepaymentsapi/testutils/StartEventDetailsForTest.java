package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.testutils;

import static uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.testutils.CaseDetailsForTest.generateValidCaseDetailWithAsylumCase;

import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd.StartEventDetails;

public class StartEventDetailsForTest extends StartEventDetails {
    public static StartEventDetails generateValidUpdatePaymentStatusDetail() {
        return new StartEventDetails(
            Event.UPDATE_PAYMENT_STATUS,
            "ccdIntegrationEventToken",
            generateValidCaseDetailWithAsylumCase()
        );
    }

}
