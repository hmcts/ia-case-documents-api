package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.roleassignment;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.roleassignment.UnknownDefaultValueDeserializer;

@JsonDeserialize(using = UnknownDefaultValueDeserializer.class)
public enum RoleType {
    CASE, ORGANISATION, @JsonEnumDefaultValue UNKNOWN
}
