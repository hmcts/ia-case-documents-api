package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.roleassignment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record RoleAssignmentResource(List<Assignment> roleAssignmentResponse) {
    @JsonCreator
    public RoleAssignmentResource(@JsonProperty("roleAssignmentResponse") List<Assignment> roleAssignmentResponse) {
        this.roleAssignmentResponse = roleAssignmentResponse;
    }

}
