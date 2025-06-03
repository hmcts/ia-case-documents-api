package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers.advice;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.time.LocalDateTime;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentServiceResponseException;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentStitchingErrorResponseException;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.service.exceptions.BadRequestException;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.service.exceptions.ExceptionResponse;

@Slf4j
@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class CallbackControllerAdvice extends ResponseEntityExceptionHandler {

    private ErrorResponseLogger errorResponseLogger;

    public CallbackControllerAdvice(ErrorResponseLogger errorResponseLogger) {
        this.errorResponseLogger = errorResponseLogger;
    }

    @ExceptionHandler({
        IllegalStateException.class,
        IllegalArgumentException.class,
        RequiredFieldMissingException.class
    })
    protected ResponseEntity<String> handleExceptions(HttpServletRequest request, Exception e) {
        log.error("Exception for the CCDCaseId: {}",
            RequestContextHolder.currentRequestAttributes().getAttribute("CCDCaseId", RequestAttributes.SCOPE_REQUEST));
        ExceptionUtils.printRootCauseStackTrace(e);

        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({BadRequestException.class, ResponseStatusException.class})
    protected ResponseEntity<ExceptionResponse> handleExceptions(ResponseStatusException ex) {

        ExceptionResponse response =
            new ExceptionResponse("BAD_REQUEST", ex.getMessage(), LocalDateTime.now());
        ExceptionUtils.printRootCauseStackTrace(ex);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DocumentStitchingErrorResponseException.class)
    protected ResponseEntity<String> handleDocumentStitchingErrorResponseException(
        HttpServletRequest request,
        DocumentStitchingErrorResponseException ex
    ) {
        log.error("Exception for the CCDCaseId: {}",
            RequestContextHolder.currentRequestAttributes().getAttribute("CCDCaseId", RequestAttributes.SCOPE_REQUEST));
        errorResponseLogger.maybeLogErrorsListResponse(ex.getStitchingServiceResponse());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DocumentServiceResponseException.class)
    protected ResponseEntity<String> handleDocumentServiceResponseException(
        HttpServletRequest request,
        DocumentServiceResponseException ex
    ) {
        log.error("Exception for the CCDCaseId: {}",
            RequestContextHolder.currentRequestAttributes().getAttribute("CCDCaseId", RequestAttributes.SCOPE_REQUEST));
        errorResponseLogger.maybeLogException(ex.getCause());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({AccessDeniedException.class})
    protected ResponseEntity<ExceptionResponse> handleAccessDeniedExceptions(AccessDeniedException ex) {
        log.error("Service name from S2S token ('ServiceAuthorization' header) is invalid", ex);
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}
