package uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures;

import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.CaseDetailsForTest.generateValidCaseDetailWithAsylumCase;

import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.StartEventDetails;

public class StartEventDetailsForTest extends StartEventDetails {
    public static StartEventDetails generateValidUpdatePaymentStatusDetail() {
        return new StartEventDetails(
            Event.UPDATE_PAYMENT_STATUS,
            "ccdIntegrationEventToken",
            generateValidCaseDetailWithAsylumCase()
        );
    }

}
