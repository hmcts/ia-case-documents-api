package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.model.refdata;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@AllArgsConstructor
public class CourtVenue {

    private String siteName;
    private String courtName;
    private String epimmsId;
    private String courtStatus;
    private String isHearingLocation;
    private String isCaseManagementLocation;
    private String courtAddress;
    private String postcode;

}
