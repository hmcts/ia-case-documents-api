package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class ErrorResponseLoggerTest {

    @Mock
    private PreSubmitCallbackResponse preSubmitCallbackResponse;

    @Mock
    private RestClientResponseException restClientResponseException;

    @InjectMocks
    private ErrorResponseLogger errorResponseLogger;

    private ListAppender<ILoggingEvent> listAppender;

    @Before
    public void setup() {

        Logger responseLogger = (Logger) LoggerFactory.getLogger(ErrorResponseLogger.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        responseLogger.addAppender(listAppender);

    }

    @Test
    public void should_handle_callback_response_with_errors_and_log_correctly() {

        String error1 = "THIS IS AN ERROR!";
        String error2 = "THIS IS ANOTHER ERROR!";

        when(preSubmitCallbackResponse.getErrors()).thenReturn(ImmutableSet.of(error1, error2));

        errorResponseLogger.maybeLogErrorsListResponse(preSubmitCallbackResponse);

        List<ILoggingEvent> logEvents = this.listAppender.list;
        assertThat(logEvents.size()).isEqualTo(2);

        assertThat(logEvents.get(0).getFormattedMessage()).isEqualTo(error1);
        assertThat(logEvents.get(1).getFormattedMessage()).isEqualTo(error2);

        verify(preSubmitCallbackResponse).getErrors();

    }

    @Test
    public void should_handle_callback_response_with_no_errors() {

        when(preSubmitCallbackResponse.getErrors()).thenReturn(ImmutableSet.of());

        errorResponseLogger.maybeLogErrorsListResponse(preSubmitCallbackResponse);

        List<ILoggingEvent> logEvents = this.listAppender.list;
        assertThat(logEvents.size()).isEqualTo(0);

        verify(preSubmitCallbackResponse).getErrors();

    }

    @Test
    public void should_handle_rest_client_exception_response_and_log() {

        String jsonResponseBody = "{}";

        when(restClientResponseException.getRawStatusCode()).thenReturn(HttpStatus.BAD_GATEWAY.value());
        when(restClientResponseException.getResponseBodyAsString()).thenReturn(jsonResponseBody);

        errorResponseLogger.maybeLogException(restClientResponseException);

        List<ILoggingEvent> logEvents = this.listAppender.list;
        assertThat(logEvents.size()).isEqualTo(1);

        assertThat(logEvents.get(0).getFormattedMessage())
            .startsWith("Error returned with status: "
                        + HttpStatus.BAD_GATEWAY.value()
                        + " \nwith response body: "
                        + jsonResponseBody);

        verify(restClientResponseException).getRawStatusCode();
        verify(restClientResponseException).getResponseBodyAsString();
    }

    @Test
    public void should_ignore_generic_exception_response() {

        Exception exception = mock(Exception.class);

        errorResponseLogger.maybeLogException(exception);

        List<ILoggingEvent> logEvents = this.listAppender.list;
        assertThat(logEvents.size()).isEqualTo(0);

        verifyNoInteractions(restClientResponseException);

    }

}
