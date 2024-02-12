package uk.gov.hmcts.reform.iacasepaymentsapi.testutils;

import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.StartEventDetails;

import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.CaseDetailsForTest.generateValidCaseDetailWithAsylumCase;

public class StartEventDetailsForTest extends StartEventDetails {
    public static StartEventDetails generateValidUpdatePaymentStatusDetail() {
        return new StartEventDetails(
            Event.UPDATE_PAYMENT_STATUS,
            "ccdIntegrationEventToken",
            generateValidCaseDetailWithAsylumCase()
        );
    }

}
