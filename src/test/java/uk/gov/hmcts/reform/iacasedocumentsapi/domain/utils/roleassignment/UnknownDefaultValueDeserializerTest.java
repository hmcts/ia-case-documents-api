package uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.roleassignment;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnknownDefaultValueDeserializerTest {

    @Getter
    enum TestEnumWithValue {
        FOO("foo"),
        BAR("bar"),
        UNKNOWN("unknown");

        private final String value;

        TestEnumWithValue(String value) {
            this.value = value;
        }
    }

    enum TestEnumNoValue {
        FOO, BAR, UNKNOWN
    }

    enum TestEnumNoUnknown {
        FOO, BAR
    }

    @Test
    void should_deserialize_using_getValue() throws IOException {
        UnknownDefaultValueDeserializer deserializer = new UnknownDefaultValueDeserializer();
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext ctx = mock(DeserializationContext.class);

        when(parser.getText()).thenReturn("foo");
        when(ctx.getContextualType()).thenReturn(
            com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(TestEnumWithValue.class)
        );

        Object result = deserializer.deserialize(parser, ctx);
        assertEquals(TestEnumWithValue.FOO, result);
    }

    @Test
    void should_deserialize_using_name() throws IOException {
        UnknownDefaultValueDeserializer deserializer = new UnknownDefaultValueDeserializer();
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext ctx = mock(DeserializationContext.class);

        when(parser.getText()).thenReturn("BAR");
        when(ctx.getContextualType()).thenReturn(
            com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(TestEnumNoValue.class)
        );

        Object result = deserializer.deserialize(parser, ctx);
        assertEquals(TestEnumNoValue.BAR, result);
    }

    @Test
    void should_return_unknown_for_unmatched_value() throws IOException {
        UnknownDefaultValueDeserializer deserializer = new UnknownDefaultValueDeserializer();
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext ctx = mock(DeserializationContext.class);

        when(parser.getText()).thenReturn("notfound");
        when(ctx.getContextualType()).thenReturn(
            com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(TestEnumWithValue.class)
        );

        Object result = deserializer.deserialize(parser, ctx);
        assertEquals(TestEnumWithValue.UNKNOWN, result);
    }

    @Test
    void should_return_null_if_no_unknown_constant() throws IOException {
        UnknownDefaultValueDeserializer deserializer = new UnknownDefaultValueDeserializer();
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext ctx = mock(DeserializationContext.class);

        when(parser.getText()).thenReturn("notfound");
        when(ctx.getContextualType()).thenReturn(
            com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(TestEnumNoUnknown.class)
        );

        Object result = deserializer.deserialize(parser, ctx);
        assertNull(result);
    }

    @Test
    void should_fallback_to_name_if_getValue_not_present() throws IOException {
        UnknownDefaultValueDeserializer deserializer = new UnknownDefaultValueDeserializer();
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext ctx = mock(DeserializationContext.class);

        when(parser.getText()).thenReturn("FOO");
        when(ctx.getContextualType()).thenReturn(
            com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(TestEnumNoValue.class)
        );

        Object result = deserializer.deserialize(parser, ctx);
        assertEquals(TestEnumNoValue.FOO, result);
    }
}