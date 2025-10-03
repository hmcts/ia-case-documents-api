package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.fee;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.LegRepAddressUk;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganisationEntityResponse {

    private String organisationIdentifier;
    private String name;
    private String status;
    private String sraId;
    private String sraRegulated;
    private String companyNumber;
    private String companyUrl;
    private SuperUser superUser;
    @Getter
    private List<String> paymentAccount;
    private List<LegRepAddressUk> contactInformation;

    public OrganisationEntityResponse(String name) {
        this.name = name;
    }

}
