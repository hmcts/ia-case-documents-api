package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers.advice;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentBundlingErrorResponseException;

@Slf4j
@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class CallbackControllerAdvice {

    @ExceptionHandler(RequiredFieldMissingException.class)
    protected ResponseEntity<String> handleRequiredFieldMissingException(
        HttpServletRequest request,
        RequiredFieldMissingException e
    ) {
        log.info("handling exception: {}", e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DocumentBundlingErrorResponseException.class)
    protected ResponseEntity<String> handleDocumentBundlingErrorResponseException(
        HttpServletRequest request,
        DocumentBundlingErrorResponseException e
    ) {
        log.info("Handling Bundling Exception {}.", e.getMessage());
        maybeLogErrorsList(e.getBundlingServiceResponse());

        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private <T extends CaseData> void maybeLogErrorsList(PreSubmitCallbackResponse<T> responseData) {
        Optional.ofNullable(responseData)
            .ifPresent(response -> response
                .getErrors()
                .forEach(log::error));

    }

}
