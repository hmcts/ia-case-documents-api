package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.caselinking;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
@ToString
public class ReasonForLink {
    @JsonProperty("Reason")
    String reason;
}
