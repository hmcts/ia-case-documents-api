package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd.callback;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Optional;
import lombok.Getter;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd.Event;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Callback<T extends CaseData> {

    @Getter
    @JsonProperty("event_id")
    private Event event;

    private CaseDetails<T> caseDetails;
    private Optional<CaseDetails<T>> caseDetailsBefore;

    private Callback() {
        // noop -- for deserializer
    }

    public Callback(
        CaseDetails<T> caseDetails,
        Optional<CaseDetails<T>> caseDetailsBefore,
        Event event
    ) {
        this.caseDetails = caseDetails;
        this.caseDetailsBefore = caseDetailsBefore;
        this.event = event;
    }

    public CaseDetails<T> getCaseDetails() {
        requireNonNull(caseDetails);
        return caseDetails;
    }

    public Optional<CaseDetails<T>> getCaseDetailsBefore() {
        requireNonNull(caseDetailsBefore);
        return caseDetailsBefore;
    }
}
