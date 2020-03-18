package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.controllers.advice;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.RequiredFieldMissingException;

@Slf4j
@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.controllers")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class CallbackControllerAdvice extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CallbackControllerAdvice.class);

    @ExceptionHandler({
        IllegalStateException.class,
        IllegalArgumentException.class,
        RequiredFieldMissingException.class
    })
    protected ResponseEntity<String> handleRequiredFieldMissingException(
        HttpServletRequest request,
        RequiredFieldMissingException e
    ) {
        log.error("Exception for the CCDCaseId: {}",
            RequestContextHolder.currentRequestAttributes().getAttribute("CCDCaseId", RequestAttributes.SCOPE_REQUEST));
        ExceptionUtils.printRootCauseStackTrace(e);

        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
