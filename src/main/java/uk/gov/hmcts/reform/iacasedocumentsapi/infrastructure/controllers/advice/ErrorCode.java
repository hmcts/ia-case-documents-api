package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers.advice;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INTERNAL_ERROR("INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"),
    BAD_REQUEST("BAD_REQUEST", HttpStatus.BAD_REQUEST, "Invalid request"),
    VALIDATION_ERROR("VALIDATION_ERROR", HttpStatus.BAD_REQUEST, "Validation failed"),
    REQUIRED_FIELD_MISSING("REQUIRED_FIELD_MISSING", HttpStatus.BAD_REQUEST, "Required field is missing"),
    DOCUMENT_SERVICE_ERROR("DOCUMENT_SERVICE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "Document service error"),
    DOCUMENT_STITCHING_ERROR("DOCUMENT_STITCHING_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "Document stitching error"),
    IDENTITY_MANAGER_ERROR("IDENTITY_MANAGER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "Identity manager error"),
    ACCESS_DENIED("ACCESS_DENIED", HttpStatus.FORBIDDEN, "Access denied"),
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Unauthorized access");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
