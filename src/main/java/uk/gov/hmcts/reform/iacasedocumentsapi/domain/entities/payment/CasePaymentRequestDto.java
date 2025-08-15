package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CasePaymentRequestDto {
    @JsonProperty("action")
    private String action;
    @JsonProperty("responsible_party")
    private String responsibleParty;
}
