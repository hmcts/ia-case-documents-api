package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.exceptions.ExceptionResponse;

@ExtendWith(MockitoExtension.class)
class CallbackControllerAdviceTest {

    @Mock
    HttpServletRequest request;
    private CallbackControllerAdvice callbackControllerAdvice;

    @BeforeEach
    public void setUp() {
        callbackControllerAdvice = new CallbackControllerAdvice();

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void should_handle_required_404_exception() {

        ResponseEntity<ExceptionResponse> responseEntity = callbackControllerAdvice
            .handleExceptions(
                new ResponseStatusException(HttpStatus.valueOf(404), "Error in calling the client method:someMethod"));

        assertEquals(responseEntity.getStatusCode().value(), HttpStatus.BAD_REQUEST.value());
        assertThat(responseEntity.getBody().getErrorMessage())
            .contains("404 NOT_FOUND \"Error in calling the client method:someMethod");
    }

    @Test
    void should_handle_required_400_exception() {

        ResponseEntity<ExceptionResponse> responseEntity = callbackControllerAdvice
            .handleExceptions(
                new ResponseStatusException(HttpStatus.valueOf(400), "Error in calling the client method:someMethod"));

        assertEquals(responseEntity.getStatusCode().value(), HttpStatus.BAD_REQUEST.value());
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
