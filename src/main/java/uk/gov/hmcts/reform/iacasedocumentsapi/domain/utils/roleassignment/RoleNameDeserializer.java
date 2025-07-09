package uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.roleassignment;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.roleassignment.RoleName;

public class RoleNameDeserializer extends JsonDeserializer<RoleName> {
    @Override
    public RoleName deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        for (RoleName roleName : RoleName.values()) {
            if (roleName.getValue().equals(value)) {
                return roleName;
            }
        }
        return RoleName.UNKNOWN;
    }
}