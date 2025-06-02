package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.service.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ExceptionResponseTest {

    @Test
    void should_hold_onto_values() {

        LocalDateTime localDateTime = LocalDateTime.now();
        String errorCode = "errorCode";
        String errorMessage = "errorMessage";
        ExceptionResponse exceptionResponse = new ExceptionResponse(errorCode, errorMessage, localDateTime);

        assertEquals(exceptionResponse.getErrorMessage(), errorMessage);
        assertEquals(exceptionResponse.getErrorCode(), errorCode);
        assertTrue(exceptionResponse.getTimestamp().isBefore(LocalDateTime.now())
                   || exceptionResponse.getTimestamp().isEqual(LocalDateTime.now()));
    }
}
