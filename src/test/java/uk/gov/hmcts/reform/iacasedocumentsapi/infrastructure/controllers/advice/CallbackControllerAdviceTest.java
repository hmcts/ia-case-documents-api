package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.time.Instant;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentServiceResponseException;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentStitchingErrorResponseException;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers.advice.model.ErrorResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.idam.IdentityManagerResponseException;

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
    private ErrorResponseBuilder errorResponseBuilder;

    private CallbackControllerAdvice callbackControllerAdvice;

    private ListAppender<ILoggingEvent> listAppender;

    private String testExceptionMessage;
    private static final String TEST_PATH = "/test/path";
    private static final String TEST_CORRELATION_ID = "test-correlation-id";

    @BeforeEach
    void setup() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpServletRequest));

        callbackControllerAdvice = new CallbackControllerAdvice(errorResponseBuilder, errorResponseLogger);

        Logger controllerAdviceLogger = (Logger) LoggerFactory.getLogger(CallbackControllerAdvice.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        controllerAdviceLogger.addAppender(listAppender);

        testExceptionMessage = "Test exception message!";
    }

    @Test
    void should_handle_required_field_missing_exception() {
        RequiredFieldMissingException ex = new RequiredFieldMissingException(testExceptionMessage);
        String expectedMessage = "Required field is missing: " + testExceptionMessage;
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.REQUIRED_FIELD_MISSING, expectedMessage);

        when(errorResponseBuilder.build(ErrorCode.REQUIRED_FIELD_MISSING, httpServletRequest, expectedMessage))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            callbackControllerAdvice.handleRequiredFieldMissingException(httpServletRequest, ex);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ErrorCode.REQUIRED_FIELD_MISSING.getCode());

        verify(errorResponseBuilder).logError(ex, ErrorCode.REQUIRED_FIELD_MISSING, httpServletRequest);
    }

    @Test
    void should_handle_illegal_state_exception() {
        IllegalStateException ex = new IllegalStateException(testExceptionMessage);
        String expectedMessage = "Invalid application state: " + testExceptionMessage;
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.BAD_REQUEST, expectedMessage);

        when(errorResponseBuilder.build(ErrorCode.BAD_REQUEST, httpServletRequest, expectedMessage))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            callbackControllerAdvice.handleIllegalStateException(httpServletRequest, ex);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST.getCode());

        verify(errorResponseBuilder).logError(ex, ErrorCode.BAD_REQUEST, httpServletRequest);
    }

    @Test
    void should_handle_illegal_argument_exception() {
        IllegalArgumentException ex = new IllegalArgumentException(testExceptionMessage);
        String expectedMessage = "Invalid argument: " + testExceptionMessage;
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.BAD_REQUEST, expectedMessage);

        when(errorResponseBuilder.build(ErrorCode.BAD_REQUEST, httpServletRequest, expectedMessage))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            callbackControllerAdvice.handleIllegalArgumentException(httpServletRequest, ex);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST.getCode());

        verify(errorResponseBuilder).logError(ex, ErrorCode.BAD_REQUEST, httpServletRequest);
    }

    @Test
    void should_handle_http_message_not_readable_exception() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        String expectedMessage = "Malformed request body";
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.BAD_REQUEST, expectedMessage);

        when(errorResponseBuilder.build(ErrorCode.BAD_REQUEST, httpServletRequest, expectedMessage))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            callbackControllerAdvice.handleHttpMessageNotReadableException(httpServletRequest, ex);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST.getCode());

        verify(errorResponseBuilder).logError(ex, ErrorCode.BAD_REQUEST, httpServletRequest);
    }

    @Test
    void should_handle_missing_servlet_request_parameter_exception() throws Exception {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("paramName", "String");
        String expectedMessage = "Missing request parameter: paramName";
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.BAD_REQUEST, expectedMessage);

        when(errorResponseBuilder.build(ErrorCode.BAD_REQUEST, httpServletRequest, expectedMessage))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            callbackControllerAdvice.handleMissingServletRequestParameterException(httpServletRequest, ex);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isNotNull();

        verify(errorResponseBuilder).logError(ex, ErrorCode.BAD_REQUEST, httpServletRequest);
    }

    @Test
    void should_handle_http_media_type_not_supported_exception() {
        HttpMediaTypeNotSupportedException ex = mock(HttpMediaTypeNotSupportedException.class);
        when(ex.getContentType()).thenReturn(null);
        String expectedMessage = "Unsupported media type: null";
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.BAD_REQUEST, expectedMessage);

        when(errorResponseBuilder.build(ErrorCode.BAD_REQUEST, httpServletRequest, expectedMessage))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            callbackControllerAdvice.handleHttpMediaTypeNotSupportedException(httpServletRequest, ex);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        verify(errorResponseBuilder).logError(ex, ErrorCode.BAD_REQUEST, httpServletRequest);
    }

    @Test
    void should_handle_http_request_method_not_supported_exception() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("DELETE");
        String expectedMessage = "HTTP method not supported: DELETE";
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.BAD_REQUEST, expectedMessage);

        when(errorResponseBuilder.build(ErrorCode.BAD_REQUEST, httpServletRequest, expectedMessage))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            callbackControllerAdvice.handleHttpRequestMethodNotSupportedException(httpServletRequest, ex);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        verify(errorResponseBuilder).logError(ex, ErrorCode.BAD_REQUEST, httpServletRequest);
    }

    @Test
    void should_handle_stitching_exception_and_log_message_correctly() {
        doNothing().when(errorResponseLogger).maybeLogErrorsListResponse(any());

        DocumentStitchingErrorResponseException ex =
            new DocumentStitchingErrorResponseException(testExceptionMessage, preSubmitCallbackResponse);
        String expectedMessage = "Document stitching failed: " + testExceptionMessage;
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.DOCUMENT_STITCHING_ERROR, expectedMessage);

        when(errorResponseBuilder.build(ErrorCode.DOCUMENT_STITCHING_ERROR, httpServletRequest, expectedMessage))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            callbackControllerAdvice.handleDocumentStitchingErrorResponseException(httpServletRequest, ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ErrorCode.DOCUMENT_STITCHING_ERROR.getCode());

        verify(errorResponseBuilder).logError(ex, ErrorCode.DOCUMENT_STITCHING_ERROR, httpServletRequest);
        verify(errorResponseLogger).maybeLogErrorsListResponse(preSubmitCallbackResponse);
    }

    @Test
    void should_handle_document_service_exception() {
        DocumentServiceResponseException ex = mock(DocumentServiceResponseException.class);
        when(ex.getMessage()).thenReturn(testExceptionMessage);

        String expectedMessage = "Document service error: " + testExceptionMessage;
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.DOCUMENT_SERVICE_ERROR, expectedMessage);
        when(errorResponseBuilder.build(ErrorCode.DOCUMENT_SERVICE_ERROR, httpServletRequest, expectedMessage))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            callbackControllerAdvice.handleDocumentServiceResponseException(httpServletRequest, ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ErrorCode.DOCUMENT_SERVICE_ERROR.getCode());

        verify(errorResponseBuilder).logError(ex, ErrorCode.DOCUMENT_SERVICE_ERROR, httpServletRequest);
        verify(errorResponseLogger).maybeLogException(any());
    }

    @Test
    void should_handle_document_service_exception_when_cause_is_null() {
        DocumentServiceResponseException ex = new DocumentServiceResponseException(testExceptionMessage);
        String expectedMessage = "Document service error: " + testExceptionMessage;
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.DOCUMENT_SERVICE_ERROR, expectedMessage);

        when(errorResponseBuilder.build(ErrorCode.DOCUMENT_SERVICE_ERROR, httpServletRequest, expectedMessage))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            callbackControllerAdvice.handleDocumentServiceResponseException(httpServletRequest, ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ErrorCode.DOCUMENT_SERVICE_ERROR.getCode());

        verify(errorResponseBuilder).logError(ex, ErrorCode.DOCUMENT_SERVICE_ERROR, httpServletRequest);
    }

    @Test
    void should_handle_identity_manager_exception() {
        IdentityManagerResponseException ex = new IdentityManagerResponseException(testExceptionMessage, new RuntimeException());
        String expectedMessage = "Authentication service unavailable";
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.IDENTITY_MANAGER_ERROR, expectedMessage);

        when(errorResponseBuilder.build(ErrorCode.IDENTITY_MANAGER_ERROR, httpServletRequest, expectedMessage))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            callbackControllerAdvice.handleIdentityManagerResponseException(httpServletRequest, ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ErrorCode.IDENTITY_MANAGER_ERROR.getCode());

        verify(errorResponseBuilder).logError(ex, ErrorCode.IDENTITY_MANAGER_ERROR, httpServletRequest);
    }

    @Test
    void should_handle_access_denied_exception() {
        AccessDeniedException ex = new AccessDeniedException(testExceptionMessage);
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.ACCESS_DENIED, ErrorCode.ACCESS_DENIED.getDefaultMessage());

        when(errorResponseBuilder.build(ErrorCode.ACCESS_DENIED, httpServletRequest, null))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            callbackControllerAdvice.handleAccessDeniedException(httpServletRequest, ex);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED.getCode());

        verify(errorResponseBuilder).logError(ex, ErrorCode.ACCESS_DENIED, httpServletRequest);
    }

    @Test
    void should_handle_all_uncaught_exceptions() {
        RuntimeException ex = new RuntimeException(testExceptionMessage);
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getDefaultMessage());

        when(errorResponseBuilder.build(ErrorCode.INTERNAL_ERROR, httpServletRequest, null))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            callbackControllerAdvice.handleAllUncaughtExceptions(httpServletRequest, ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ErrorCode.INTERNAL_ERROR.getCode());

        verify(errorResponseBuilder).logError(ex, ErrorCode.INTERNAL_ERROR, httpServletRequest);
    }

    private ErrorResponse buildErrorResponse(ErrorCode errorCode, String message) {
        return ErrorResponse.builder()
            .errorCode(errorCode.getCode())
            .message(message)
            .timestamp(Instant.now())
            .requestId(TEST_CORRELATION_ID)
            .path(TEST_PATH)
            .build();
    }
}
