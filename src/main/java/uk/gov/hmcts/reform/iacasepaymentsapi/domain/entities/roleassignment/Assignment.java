package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.roleassignment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class Assignment {

    @Getter
    private final String id;
    @Getter
    private final LocalDateTime created;
    @Getter
    private final List<String> authorisations;
    @Getter
    private final ActorIdType actorIdType;
    @Getter
    private final String actorId;
    @Getter
    private final RoleType roleType;
    @Getter
    private final RoleName roleName;
    @Getter
    private final RoleCategory roleCategory;
    @Getter
    private final Classification classification;
    @Getter
    private final GrantType grantType;
    private final Boolean readOnly;
    @Getter
    private final Map<String, String> attributes;

    @JsonCreator
    public Assignment(@JsonProperty("id") String id,
                      @JsonProperty("created") LocalDateTime created,
                      @JsonProperty("authorisations") List<String> authorisations,
                      @JsonProperty("actorIdType") ActorIdType actorIdType,
                      @JsonProperty("actorId") String actorId,
                      @JsonProperty("roleType") RoleType roleType,
                      @JsonProperty("roleName") RoleName roleName,
                      @JsonProperty("roleCategory") RoleCategory roleCategory,
                      @JsonProperty("classification") Classification classification,
                      @JsonProperty("grantType") GrantType grantType,
                      @JsonProperty("readOnly") Boolean readOnly,
                      @JsonProperty("attributes") Map<String, String> attributes) {
        this.id = id;
        this.created = created;
        this.authorisations = authorisations;
        this.actorIdType = actorIdType;
        this.actorId = actorId;
        this.roleType = roleType;
        this.roleName = roleName;
        this.roleCategory = roleCategory;
        this.classification = classification;
        this.grantType = grantType;
        this.readOnly = readOnly;
        this.attributes = attributes;
    }

}
