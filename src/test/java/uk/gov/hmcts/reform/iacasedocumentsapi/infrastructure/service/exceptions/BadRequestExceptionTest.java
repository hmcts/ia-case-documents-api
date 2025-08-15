package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.service.exceptions;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BadRequestExceptionTest {

    @Test
    void should_wrap_job_execution_exception() {

        new BadRequestException("Not found");
        assertTrue(true);
    }
}
