package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.exceptions;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BadRequestExceptionTest {

    @Test
    void should_wrap_job_execution_exception() {

        assertTrue(new BadRequestException("Not found") instanceof BadRequestException);
    }
}
