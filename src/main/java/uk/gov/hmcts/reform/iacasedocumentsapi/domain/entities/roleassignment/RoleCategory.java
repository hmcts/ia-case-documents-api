package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.roleassignment;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum RoleCategory {
    ADMIN, JUDICIAL,  STAFF, LEGAL_OPERATIONS, PROFESSIONAL, CITIZEN, @JsonEnumDefaultValue UNKNOWN
}
