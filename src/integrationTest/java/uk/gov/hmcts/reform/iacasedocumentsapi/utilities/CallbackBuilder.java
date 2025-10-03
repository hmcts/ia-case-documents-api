package uk.gov.hmcts.reform.iacasedocumentsapi.utilities;

import java.util.Optional;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;


public class CallbackBuilder {

    public static CallbackBuilder callbackBuilder() {
        return new CallbackBuilder();
    }

    private Event event;
    private CaseDetails<AsylumCase> caseDetails;
    private Optional<CaseDetails<AsylumCase>> caseDetailsBefore;

    public CallbackBuilder event(Event event) {
        this.event = event;
        return this;
    }

    public CallbackBuilder caseDetails(CaseDetails<AsylumCase> caseDetails) {
        this.caseDetails = caseDetails;
        return this;
    }

    public CallbackBuilder caseDetailsBefore(Optional<CaseDetails<AsylumCase>> caseDetailsBefore) {
        this.caseDetailsBefore = caseDetailsBefore;
        return this;
    }

    public Callback<AsylumCase> build() {
        return new Callback<>(caseDetails, caseDetailsBefore, event);
    }

    public String toString() {
        return "Callback.CallbackBuilder(event=" + this.event + ", caseDetails=" + this.caseDetails + ", caseDetailsBefore=" + this.caseDetailsBefore + ")";
    }
}
