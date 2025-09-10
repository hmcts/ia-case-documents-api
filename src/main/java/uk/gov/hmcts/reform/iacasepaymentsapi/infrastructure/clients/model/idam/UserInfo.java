package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.model.idam;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserInfo {

    @JsonProperty("sub")
    private String email;
    private String uid;
    @Setter
    private List<String> roles;
    private String name;
    private String givenName;
    private String familyName;
}
