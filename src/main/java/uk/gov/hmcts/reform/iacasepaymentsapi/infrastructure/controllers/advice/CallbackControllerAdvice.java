package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers.advice;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.exceptions.BadRequestException;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.exceptions.ExceptionResponse;


@Slf4j
@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class CallbackControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler({BadRequestException.class, ResponseStatusException.class})
    protected ResponseEntity<ExceptionResponse> handleExceptions(ResponseStatusException ex) {

        ExceptionResponse response =
            new ExceptionResponse("BAD_REQUEST", ex.getMessage(), LocalDateTime.now());
        ExceptionUtils.printRootCauseStackTrace(ex);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
