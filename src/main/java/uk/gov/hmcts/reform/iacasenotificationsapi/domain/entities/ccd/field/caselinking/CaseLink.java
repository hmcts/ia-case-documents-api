package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.caselinking;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
@ToString
public class CaseLink {
    @JsonProperty("CaseReference")
    String caseReference;
    @JsonProperty("CaseType")
    String caseType;
    @JsonProperty("CreatedDatetime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdDateTime;
    @JsonProperty("ReasonForLink")
    List<IdValue<ReasonForLink>> reasonsForLink;
}
