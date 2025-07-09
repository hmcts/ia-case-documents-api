package uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.roleassignment;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.roleassignment.RoleName;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleNameDeserializerTest {

    @Test
    void should_deserialize_matching_value() throws IOException {
        RoleNameDeserializer deserializer = new RoleNameDeserializer();
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext ctx = mock(DeserializationContext.class);

        when(parser.getText()).thenReturn("tribunal-caseworker");
        RoleName result = deserializer.deserialize(parser, ctx);
        assertEquals(RoleName.TRIBUNAL_CASEWORKER, result);

        when(parser.getText()).thenReturn("not-a-role");
        result = deserializer.deserialize(parser, ctx);

        assertEquals(RoleName.UNKNOWN, result);
    }

    @Test
    void should_return_UNKNOWN_for_null_value() throws IOException {
        RoleNameDeserializer deserializer = new RoleNameDeserializer();
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext ctx = mock(DeserializationContext.class);

        when(parser.getText()).thenReturn(null);
        RoleName result = deserializer.deserialize(parser, ctx);

        assertEquals(RoleName.UNKNOWN, result);
    }

    @Test
    void should_return_UNKNOWN_for_empty_value() throws IOException {
        RoleNameDeserializer deserializer = new RoleNameDeserializer();
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext ctx = mock(DeserializationContext.class);

        when(parser.getText()).thenReturn("");
        RoleName result = deserializer.deserialize(parser, ctx);

        assertEquals(RoleName.UNKNOWN, result);
    }
}