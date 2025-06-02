package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd.callback;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Optional;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PostSubmitCallbackResponse {

    private Optional<String> confirmationHeader = Optional.empty();
    private Optional<String> confirmationBody = Optional.empty();

    public void setConfirmationHeader(String confirmationHeader) {
        this.confirmationHeader = Optional.ofNullable(confirmationHeader);
    }

    public void setConfirmationBody(String confirmationBody) {
        this.confirmationBody = Optional.ofNullable(confirmationBody);
    }

    public PostSubmitCallbackResponse() {
        // noop -- for deserializer
    }

    public PostSubmitCallbackResponse(String header, String body) {
        this.confirmationHeader = Optional.of(header);
        this.confirmationBody = Optional.of(body);
    }
}
