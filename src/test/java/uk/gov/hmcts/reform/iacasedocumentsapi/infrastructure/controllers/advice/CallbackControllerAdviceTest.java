package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.google.common.collect.ImmutableSet;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentBundlingErrorResponseException;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class CallbackControllerAdviceTest {

    @Mock
    private PreSubmitCallbackResponse preSubmitCallbackResponse;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private CallbackControllerAdvice callbackControllerAdvice;

    @Before
    public void setup() {

    }

    @Test
    public void should_handle_bundling_exception_and_log_correctly_when_errors() {
        Logger controllerAdviceLogger = (Logger) LoggerFactory.getLogger(CallbackControllerAdvice.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        controllerAdviceLogger.addAppender(listAppender);

        DocumentBundlingErrorResponseException ex = new DocumentBundlingErrorResponseException("Test exception message!", preSubmitCallbackResponse);
        String logMessage = "Handling Bundling Exception ";
        String error1 = "THIS IS AN ERROR!";
        String error2 = "THIS IS ANOTHER ERROR!";

        when(preSubmitCallbackResponse.getErrors()).thenReturn(ImmutableSet.of(error1, error2));

        ResponseEntity<String> responseEntity =
            callbackControllerAdvice.handleDocumentBundlingErrorResponseException(httpServletRequest, ex);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        List<ILoggingEvent> logEvents = listAppender.list;
        assertThat(logEvents.size()).isEqualTo(3);

        assertThat(logEvents.get(0).getFormattedMessage())
            .isEqualTo(logMessage + ex.getMessage() + ".");
        assertThat(logEvents.get(1).getFormattedMessage()).isEqualTo(error1);
        assertThat(logEvents.get(2).getFormattedMessage()).isEqualTo(error2);

        verify(preSubmitCallbackResponse).getErrors();

    }

    @Test
    public void should_handle_bundling_exception_and_log_correctly_when_no_errors() {

        Logger controllerAdviceLogger = (Logger) LoggerFactory.getLogger(CallbackControllerAdvice.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        controllerAdviceLogger.addAppender(listAppender);

        DocumentBundlingErrorResponseException ex = new DocumentBundlingErrorResponseException("Test exception message!", preSubmitCallbackResponse);
        String logMessage = "Handling Bundling Exception ";

        when(preSubmitCallbackResponse.getErrors()).thenReturn(ImmutableSet.of());

        ResponseEntity<String> responseEntity =
            callbackControllerAdvice.handleDocumentBundlingErrorResponseException(httpServletRequest, ex);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        List<ILoggingEvent> logEvents = listAppender.list;
        assertThat(logEvents.size()).isEqualTo(1);

        assertThat(logEvents.get(0).getFormattedMessage())
            .isEqualTo(logMessage + ex.getMessage() + ".");

        verify(preSubmitCallbackResponse).getErrors();

    }

    @Test
    public void should_handle_bundling_exception_when_null_response() {

    }


}