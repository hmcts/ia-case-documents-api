package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class SubmitEventDetails {

    private long id;
    private String jurisdiction;
    private State state;
    private Map<String, Object> data;
    private int callbackResponseStatusCode;
    private String callbackResponseStatus;
}
