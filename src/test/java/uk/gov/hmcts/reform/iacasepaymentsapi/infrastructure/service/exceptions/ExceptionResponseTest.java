package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ExceptionResponseTest {

    private String errorMessage = "errorMessage";
    private String errorCode = "errorCode";
    private LocalDateTime localDateTime;

    private ExceptionResponse exceptionResponse;

    @Test
    void should_hold_onto_values() {

        localDateTime = LocalDateTime.now();
        exceptionResponse = new ExceptionResponse(errorCode, errorMessage, localDateTime);

        assertEquals(exceptionResponse.getErrorMessage(), errorMessage);
        assertEquals(exceptionResponse.getErrorCode(), errorCode);
        assertTrue(exceptionResponse.getTimestamp().isBefore(LocalDateTime.now()));
    }
}
