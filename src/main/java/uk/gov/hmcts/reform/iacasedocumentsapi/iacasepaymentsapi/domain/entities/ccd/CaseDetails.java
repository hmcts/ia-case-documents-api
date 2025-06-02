package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CaseDetails<T extends CaseData> {

    @Getter
    private long id;
    private String jurisdiction;
    private State state;
    private T caseData;

    private CaseDetails() {
        // noop -- for deserializer
    }

    public CaseDetails(
        long id,
        String jurisdiction,
        State state,
        T caseData
    ) {
        this.id = id;
        this.jurisdiction = jurisdiction;
        this.state = state;
        this.caseData = caseData;
    }

    public String getJurisdiction() {
        requireNonNull(jurisdiction);
        return jurisdiction;
    }

    public State getState() {
        requireNonNull(state);
        return state;
    }

    public T getCaseData() {
        requireNonNull(caseData);
        return caseData;
    }
}
