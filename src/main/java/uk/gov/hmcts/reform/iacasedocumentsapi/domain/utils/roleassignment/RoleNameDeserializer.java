package uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.roleassignment;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.roleassignment.RoleName;

public class RoleNameDeserializer extends ValueDeserializer<RoleName> {
    @Override
    public RoleName deserialize(JsonParser p, DeserializationContext ctxt) {
        String value = p.getString();
        return java.util.Arrays.stream(RoleName.values())
            .filter(roleName -> roleName.getValue().equals(value))
            .findFirst()
            .orElse(RoleName.UNKNOWN);
    }
}