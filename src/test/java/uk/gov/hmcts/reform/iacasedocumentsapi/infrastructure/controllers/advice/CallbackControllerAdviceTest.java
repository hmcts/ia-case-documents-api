package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentServiceResponseException;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentStitchingErrorResponseException;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.service.exceptions.ExceptionResponse;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CallbackControllerAdviceTest {

    @Mock
    private PreSubmitCallbackResponse preSubmitCallbackResponse;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private ErrorResponseLogger errorResponseLogger;

    @Mock
    private RequiredFieldMissingException requiredFieldMissingException;

    @InjectMocks
    private CallbackControllerAdvice callbackControllerAdvice;

    private ListAppender<ILoggingEvent> listAppender;

    private String testExceptionMessage;

    @BeforeEach
    void setup() {

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpServletRequest));

        Logger controllerAdviceLogger = (Logger) LoggerFactory.getLogger(CallbackControllerAdvice.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        controllerAdviceLogger.addAppender(listAppender);

        testExceptionMessage = "Test exception message!";

    }

    @Test
    void should_handle_exception_correctly() {
        when(httpServletRequest.getAttribute("CCDCaseId")).thenReturn("Case12345");

        DocumentStitchingErrorResponseException ex =
                new DocumentStitchingErrorResponseException(testExceptionMessage, preSubmitCallbackResponse);
        String logMessage = "Exception when stitching a bundle with message: ";

        ResponseEntity<String> responseEntity =
                callbackControllerAdvice.handleExceptions(httpServletRequest, requiredFieldMissingException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        List<ILoggingEvent> logEvents = this.listAppender.list;
        assertEquals(1, logEvents.size());

        assertThat(logEvents.get(0).getFormattedMessage())
                .isLessThanOrEqualTo(logMessage + ex.getMessage() + ".");

    }

    @Test
    void should_handle_stitching_exception_and_log_message_correctly() {
        when(httpServletRequest.getAttribute("CCDCaseId")).thenReturn("Case12345");

        doNothing().when(errorResponseLogger).maybeLogErrorsListResponse(any());

        DocumentStitchingErrorResponseException ex =
            new DocumentStitchingErrorResponseException(testExceptionMessage, preSubmitCallbackResponse);
        String logMessage = "Exception when stitching a bundle with message: ";

        ResponseEntity<String> responseEntity =
            callbackControllerAdvice.handleDocumentStitchingErrorResponseException(httpServletRequest, ex);

        assertEquals(responseEntity.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);

        List<ILoggingEvent> logEvents = this.listAppender.list;
        assertEquals(logEvents.size(), 1);

        assertThat(logEvents.get(0).getFormattedMessage())
            .isLessThanOrEqualTo(logMessage + ex.getMessage() + ".");

        verify(errorResponseLogger).maybeLogErrorsListResponse(any());

    }

    @Test
    void should_handle_document_service_exception() {

        DocumentServiceResponseException ex = mock(DocumentServiceResponseException.class);

        when(ex.getMessage()).thenReturn(testExceptionMessage);

        ResponseEntity<String> responseEntity =
            callbackControllerAdvice.handleDocumentServiceResponseException(httpServletRequest, ex);

        assertEquals(responseEntity.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);

        List<ILoggingEvent> logEvents = this.listAppender.list;

        assertEquals(logEvents.size(), 1);
        String logMessage = "Document service Exception with message: ";
        assertThat(logEvents.get(0).getFormattedMessage()).isGreaterThanOrEqualTo(logMessage + testExceptionMessage + ".");

        verify(errorResponseLogger).maybeLogException(any());

    }

    @Test
    void should_handle_stitching_http_exception_when_cause_is_null() {
        String logMessage = "Document service Exception with message: ";

        DocumentServiceResponseException ex = new DocumentServiceResponseException(testExceptionMessage);
        ResponseEntity<String> responseEntity =
            callbackControllerAdvice.handleDocumentServiceResponseException(httpServletRequest, ex);

        assertEquals(responseEntity.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);

        List<ILoggingEvent> logEvents = this.listAppender.list;

        assertEquals(logEvents.size(), 1);
        assertThat(logEvents.get(0).getFormattedMessage()).isGreaterThanOrEqualTo(logMessage + ex.getMessage() + ".");

    }


    @Test
    void should_handle_required_404_exception() {

        ResponseEntity<ExceptionResponse> responseEntity = callbackControllerAdvice
            .handleExceptions(
                new ResponseStatusException(HttpStatus.valueOf(404), "Error in calling the client method:someMethod"));

        assertEquals(responseEntity.getStatusCode().value(), HttpStatus.BAD_REQUEST.value());
        assertNotNull(responseEntity.getBody());
        assertThat(responseEntity.getBody().getErrorMessage())
            .contains("404 NOT_FOUND \"Error in calling the client method:someMethod");
    }

    @Test
    void should_handle_required_400_exception() {

        ResponseEntity<ExceptionResponse> responseEntity = callbackControllerAdvice
            .handleExceptions(
                new ResponseStatusException(HttpStatus.valueOf(400), "Error in calling the client method:someMethod"));

        assertEquals(responseEntity.getStatusCode().value(), HttpStatus.BAD_REQUEST.value());
        assertNotNull(responseEntity.getBody());
        assertThat(responseEntity.getBody().getErrorMessage())
            .contains("400 BAD_REQUEST \"Error in calling the client method:someMethod\"");
    }

    @Test
    void should_handle_required_403_access_denied_exception_exception() {
        ResponseEntity<ExceptionResponse> responseEntity =
            callbackControllerAdvice.handleAccessDeniedExceptions(
                new AccessDeniedException("Invalid S2S Token...")
            );

        assertEquals(responseEntity.getStatusCode().value(), HttpStatus.FORBIDDEN.value());
    }
}
