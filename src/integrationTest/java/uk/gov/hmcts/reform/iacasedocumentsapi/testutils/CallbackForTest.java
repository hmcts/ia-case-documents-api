package uk.gov.hmcts.reform.iacasedocumentsapi.testutils;

import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;

public class CallbackForTest {

    CallbackForTest(Event event, CaseDetailsForTest caseDetails) {
    }

    public static class CallbackForTestBuilder implements Builder<CallbackForTest> {

        public static CallbackForTestBuilder callback() {
            return new CallbackForTestBuilder();
        }

        private Event event;
        private CaseDetailsForTest caseDetails;

        public CallbackForTestBuilder event(Event event) {
            this.event = event;
            return this;
        }

        public CallbackForTestBuilder caseDetails(CaseDetailsForTest.CaseDetailsForTestBuilder caseDetails) {
            this.caseDetails = caseDetails.build();
            return this;
        }

        public CallbackForTest build() {
            return new CallbackForTest(event, caseDetails);
        }
    }
}
