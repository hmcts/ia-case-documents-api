package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class PriorApplication {

    private String applicationId;
    private String caseDataJson;

    @JsonCreator
    private PriorApplication() {
        // noop -- for deserializer
    }

    public PriorApplication(String applicationId, String caseDataJson) {
        this.applicationId = applicationId;
        this.caseDataJson = caseDataJson;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getCaseDataJson() {
        return caseDataJson;
    }
}
