package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.caselinking;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

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
    @JsonProperty("CreatedDateTime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    LocalDateTime createdDateTime;
    @JsonProperty("ReasonForLink")
    List<IdValue<ReasonForLink>> reasonsForLink;
}
