package uk.gov.hmcts.reform.iacasedocumentsapi.utilities;

import java.time.LocalDateTime;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;

public class CaseDetailsBuilder {

    public static CaseDetailsBuilder caseDetailsBuilder() {
        return new CaseDetailsBuilder();
    }

    private long id;
    private String jurisdiction;
    private State state;
    private AsylumCase caseData;
    private LocalDateTime createdDate;

    public CaseDetailsBuilder id(long id) {
        this.id = id;
        return this;
    }

    public CaseDetailsBuilder jurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
        return this;
    }

    public CaseDetailsBuilder state(State state) {
        this.state = state;
        return this;
    }

    public CaseDetailsBuilder caseData(AsylumCase caseData) {
        this.caseData = caseData;
        return this;
    }

    public CaseDetailsBuilder createdDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public CaseDetails<AsylumCase> build() {
        return new CaseDetails<>(id, jurisdiction, state, caseData, createdDate);
    }

    public String toString() {
        return "CaseDetails.CaseDetailsBuilder(id=" + this.id + ", jurisdiction=" + this.jurisdiction + ", state=" + this.state + ", caseData=" + this.caseData + ", createdDate=" + this.createdDate + ")";
    }
}