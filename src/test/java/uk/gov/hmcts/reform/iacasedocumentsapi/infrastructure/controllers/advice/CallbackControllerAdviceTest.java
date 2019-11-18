package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentServiceResponseException;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentStitchingErrorResponseException;

@RunWith(MockitoJUnitRunner.class)
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

    @Before
    public void setup() {

        Logger controllerAdviceLogger = (Logger) LoggerFactory.getLogger(CallbackControllerAdvice.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        controllerAdviceLogger.addAppender(listAppender);

        testExceptionMessage = "Test exception message!";

        doNothing().when(errorResponseLogger).maybeLogErrorsListResponse(any());
        doNothing().when(errorResponseLogger).maybeLogException(any());

    }

    @Test
    public void should_handle_stitching_exception_and_log_message_correctly() {

        DocumentStitchingErrorResponseException ex =
            new DocumentStitchingErrorResponseException(testExceptionMessage, preSubmitCallbackResponse);
        String logMessage = "Exception when stitching a bundle with message: ";

        ResponseEntity<String> responseEntity =
            callbackControllerAdvice.handleDocumentStitchingErrorResponseException(httpServletRequest, ex);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        List<ILoggingEvent> logEvents = this.listAppender.list;
        assertThat(logEvents.size()).isEqualTo(1);

        assertThat(logEvents.get(0).getFormattedMessage())
            .isEqualTo(logMessage + ex.getMessage() + ".");

        verify(errorResponseLogger).maybeLogErrorsListResponse(any());

    }

    @Test
    public void should_handle_document_service_exception() {

        DocumentServiceResponseException ex = mock(DocumentServiceResponseException.class);
        String logMessage = "Document service Exception with message: ";

        when(ex.getMessage()).thenReturn(testExceptionMessage);

        ResponseEntity<String> responseEntity =
            callbackControllerAdvice.handleDocumentServiceResponseException(httpServletRequest, ex);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        List<ILoggingEvent> logEvents = this.listAppender.list;

        assertThat(logEvents.size()).isEqualTo(1);
        assertThat(logEvents.get(0).getFormattedMessage()).isEqualTo(logMessage + testExceptionMessage + ".");

        verify(errorResponseLogger).maybeLogException(any());

    }

    @Test
    public void should_handle_stitching_http_exception_when_cause_is_null() {
        String logMessage = "Document service Exception with message: ";

        DocumentServiceResponseException ex = new DocumentServiceResponseException(testExceptionMessage);
        ResponseEntity<String> responseEntity =
            callbackControllerAdvice.handleDocumentServiceResponseException(httpServletRequest, ex);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        List<ILoggingEvent> logEvents = this.listAppender.list;

        assertThat(logEvents.size()).isEqualTo(1);
        assertThat(logEvents.get(0).getFormattedMessage()).isEqualTo(logMessage + ex.getMessage() + ".");

    }

}