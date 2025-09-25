package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.exceptions;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.service.exceptions.BadRequestException;

class BadRequestExceptionTest {

    @Test
    void should_wrap_job_execution_exception() {

        assertTrue(new BadRequestException("Not found") instanceof BadRequestException);
    }
}
