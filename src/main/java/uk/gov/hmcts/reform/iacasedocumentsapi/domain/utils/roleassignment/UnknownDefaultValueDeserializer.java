package uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.roleassignment;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.lang.reflect.Method;

@SuppressWarnings({"unchecked", "rawtypes"})
public class UnknownDefaultValueDeserializer extends JsonDeserializer<Enum<?>> {

    @Override
    public Enum<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Class<?> enumClass = ctxt.getContextualType().getRawClass();
        String value = p.getText();
        Object[] constants = enumClass.getEnumConstants();
        for (Object constant : constants) {
            try {
                Method getValue = enumClass.getMethod("getValue");
                Object enumValue = getValue.invoke(constant);
                if (enumValue.equals(value)) {
                    return (Enum<?>) constant;
                }
            } catch (NoSuchMethodException e) {
                if (((Enum<?>) constant).name().equals(value)) {
                    return (Enum<?>) constant;
                }
            } catch (Exception ignored) {
            }
        }
        try {
            return Enum.valueOf((Class<Enum>) enumClass, "UNKNOWN");
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}