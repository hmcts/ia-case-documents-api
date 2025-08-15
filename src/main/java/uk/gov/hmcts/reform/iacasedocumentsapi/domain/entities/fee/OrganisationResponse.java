package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.fee;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganisationResponse {

    private OrganisationEntityResponse organisationEntityResponse;

    public OrganisationResponse(OrganisationEntityResponse organisationEntityResponse) {
        this.organisationEntityResponse = organisationEntityResponse;
    }

    public OrganisationResponse() {
    }
}
