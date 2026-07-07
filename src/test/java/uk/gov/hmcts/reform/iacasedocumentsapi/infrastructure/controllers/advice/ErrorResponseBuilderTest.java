package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers.advice.model.ErrorResponse;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class ErrorResponseBuilderTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    private ErrorResponseBuilder errorResponseBuilder;

    private ListAppender<ILoggingEvent> listAppender;

    private static final String TEST_PATH = "/test/path";
    private static final String TEST_CORRELATION_ID = "test-correlation-id";
    private static final String TEST_CCD_CASE_ID = "1234567890";

    @BeforeEach
    void setup() {
        errorResponseBuilder = new ErrorResponseBuilder();
        when(httpServletRequest.getRequestURI()).thenReturn(TEST_PATH);

        Logger builderLogger = (Logger) LoggerFactory.getLogger(ErrorResponseBuilder.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        builderLogger.addAppender(listAppender);

        MDC.put(CorrelationIdFilter.CORRELATION_ID_MDC_KEY, TEST_CORRELATION_ID);
    }

    @Test
    void should_build_error_response_with_custom_message() {
        String customMessage = "Custom error message";

        ErrorResponse response = errorResponseBuilder.build(ErrorCode.INTERNAL_ERROR, httpServletRequest, customMessage);

        assertThat(response).isNotNull();
        assertThat(response.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_ERROR.getCode());
        assertThat(response.getMessage()).isEqualTo(customMessage);
        assertThat(response.getPath()).isEqualTo(TEST_PATH);
        assertThat(response.getRequestId()).isEqualTo(TEST_CORRELATION_ID);
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void should_build_error_response_with_default_message_when_custom_is_null() {
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.INTERNAL_ERROR, httpServletRequest, null);

        assertThat(response).isNotNull();
        assertThat(response.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_ERROR.getCode());
        assertThat(response.getMessage()).isEqualTo(ErrorCode.INTERNAL_ERROR.getDefaultMessage());
    }

    @Test
    void should_build_error_response_with_field_errors() {
        List<ErrorResponse.FieldError> fieldErrors = List.of(
            ErrorResponse.FieldError.builder().field("field1").message("error1").build(),
            ErrorResponse.FieldError.builder().field("field2").message("error2").build()
        );

        ErrorResponse response = errorResponseBuilder.buildWithFieldErrors(
            ErrorCode.VALIDATION_ERROR, httpServletRequest, fieldErrors);

        assertThat(response).isNotNull();
        assertThat(response.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR.getCode());
        assertThat(response.getMessage()).isEqualTo(ErrorCode.VALIDATION_ERROR.getDefaultMessage());
        assertThat(response.getFieldErrors()).hasSize(2);
        assertThat(response.getFieldErrors().get(0).getField()).isEqualTo("field1");
        assertThat(response.getFieldErrors().get(0).getMessage()).isEqualTo("error1");
    }

    @Test
    void should_log_error_with_ccd_case_id() {
        when(httpServletRequest.getAttribute("CCDCaseId")).thenReturn(TEST_CCD_CASE_ID);
        ServletRequestAttributes attrs = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(attrs);

        RuntimeException ex = new RuntimeException("Test exception");

        errorResponseBuilder.logError(ex, ErrorCode.INTERNAL_ERROR, httpServletRequest);

        List<ILoggingEvent> logEvents = listAppender.list;
        assertThat(logEvents).hasSize(1);
        assertThat(logEvents.get(0).getFormattedMessage())
            .contains(ErrorCode.INTERNAL_ERROR.getCode())
            .contains(TEST_CCD_CASE_ID)
            .contains(TEST_PATH)
            .contains(TEST_CORRELATION_ID);
    }

    @Test
    void should_log_error_with_unknown_ccd_case_id_when_not_available() {
        RequestContextHolder.resetRequestAttributes();

        RuntimeException ex = new RuntimeException("Test exception");

        errorResponseBuilder.logError(ex, ErrorCode.INTERNAL_ERROR, httpServletRequest);

        List<ILoggingEvent> logEvents = listAppender.list;
        assertThat(logEvents).hasSize(1);
        assertThat(logEvents.get(0).getFormattedMessage()).contains("unknown");
    }
}
