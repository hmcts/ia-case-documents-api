package uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.roleassignment;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.roleassignment.RoleName;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoleNameDeserializerTest {

    @Test
    void should_deserialize_matching_value() throws IOException {
        RoleNameDeserializer deserializer = new RoleNameDeserializer();
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext ctx = mock(DeserializationContext.class);

        when(parser.getString()).thenReturn("tribunal-caseworker");
        RoleName result = deserializer.deserialize(parser, ctx);
        assertEquals(RoleName.TRIBUNAL_CASEWORKER, result);

        when(parser.getString()).thenReturn("not-a-role");
        result = deserializer.deserialize(parser, ctx);

        assertEquals(RoleName.UNKNOWN, result);
    }

    @Test
    void should_return_unknown_for_null_value() throws IOException {
        RoleNameDeserializer deserializer = new RoleNameDeserializer();
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext ctx = mock(DeserializationContext.class);

        when(parser.getString()).thenReturn(null);
        RoleName result = deserializer.deserialize(parser, ctx);

        assertEquals(RoleName.UNKNOWN, result);
    }

    @Test
    void should_return_unknown_for_empty_value() throws IOException {
        RoleNameDeserializer deserializer = new RoleNameDeserializer();
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext ctx = mock(DeserializationContext.class);

        when(parser.getString()).thenReturn("");
        RoleName result = deserializer.deserialize(parser, ctx);

        assertEquals(RoleName.UNKNOWN, result);
    }
}