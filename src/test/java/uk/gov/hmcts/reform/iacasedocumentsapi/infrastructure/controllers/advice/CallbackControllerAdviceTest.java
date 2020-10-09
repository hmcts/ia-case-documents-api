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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentServiceResponseException;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentStitchingErrorResponseException;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class CallbackControllerAdviceTest {

    @Mock
    private PreSubmitCallbackResponse preSubmitCallbackResponse;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private ErrorResponseLogger errorResponseLogger;

    @InjectMocks
    private CallbackControllerAdvice callbackControllerAdvice;

    private ListAppender<ILoggingEvent> listAppender;

    private String testExceptionMessage;

    @BeforeEach
    public void setup() {

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpServletRequest));

        Logger controllerAdviceLogger = (Logger) LoggerFactory.getLogger(CallbackControllerAdvice.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        controllerAdviceLogger.addAppender(listAppender);

        testExceptionMessage = "Test exception message!";

    }

    @Test
    public void should_handle_stitching_exception_and_log_message_correctly() {
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
    public void should_handle_document_service_exception() {

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
    public void should_handle_stitching_http_exception_when_cause_is_null() {
        String logMessage = "Document service Exception with message: ";

        DocumentServiceResponseException ex = new DocumentServiceResponseException(testExceptionMessage);
        ResponseEntity<String> responseEntity =
            callbackControllerAdvice.handleDocumentServiceResponseException(httpServletRequest, ex);

        assertEquals(responseEntity.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);

        List<ILoggingEvent> logEvents = this.listAppender.list;

        assertEquals(logEvents.size(), 1);
        assertThat(logEvents.get(0).getFormattedMessage()).isGreaterThanOrEqualTo(logMessage + ex.getMessage() + ".");

    }

}
