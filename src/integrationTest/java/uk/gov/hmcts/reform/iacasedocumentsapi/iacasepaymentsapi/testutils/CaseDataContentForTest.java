package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.testutils;

import static uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;

import java.util.Collections;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd.CaseDataContent;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd.Event;

public class CaseDataContentForTest extends CaseDataContent {

    public static CaseDataContent generateValidUpdatePaymentStatus(String paymentStatus, String caseReference, String eventToken) {
        return new CaseDataContent(
            caseReference,
            Collections.singletonMap(PAYMENT_STATUS.value(), paymentStatus),
            Collections.singletonMap("id", Event.UPDATE_PAYMENT_STATUS.toString()),
            eventToken,
            true
        );
    }

}
