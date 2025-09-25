package uk.gov.hmcts.reform.iacasedocumentsapi.utilities.fixtures;

import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.StartEventDetails;

import static uk.gov.hmcts.reform.iacasedocumentsapi.utilities.fixtures.CaseDetailsForTest.generateValidCaseDetailWithAsylumCase;

public class StartEventDetailsForTest extends StartEventDetails {
    public static StartEventDetails generateValidUpdatePaymentStatusDetail() {
        return new StartEventDetails(
            Event.UPDATE_PAYMENT_STATUS,
            "ccdIntegrationEventToken",
            generateValidCaseDetailWithAsylumCase()
        );
    }

}
