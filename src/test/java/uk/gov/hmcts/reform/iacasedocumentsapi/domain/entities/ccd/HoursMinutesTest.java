package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HoursMinutesTest {

    @Test
    void should_instantiate_with_values() {
        HoursMinutes object = new HoursMinutes(2, 10);
        assertEquals(2, object.getHours());
        assertEquals(10, object.getMinutes());
    }

    @Test
    void should_instantiate_with_minutes_alone() {
        HoursMinutes object1 = new HoursMinutes(0, 80);
        assertEquals(1, object1.getHours());
        assertEquals(20, object1.getMinutes());

        HoursMinutes object2 = new HoursMinutes(80);
        assertEquals(1, object1.getHours());
        assertEquals(20, object1.getMinutes());
    }

    @Test
    void should_instantiate_with_null_values() {
        HoursMinutes object1 = new HoursMinutes(2, null);
        assertEquals(2, object1.getHours());
        assertEquals(0, object1.getMinutes());

        HoursMinutes object2 = new HoursMinutes(null, 15);
        assertEquals(0, object2.getHours());
        assertEquals(15, object2.getMinutes());

        HoursMinutes object3 = new HoursMinutes(null, null);
        assertEquals(0, object3.getHours());
        assertEquals(0, object3.getMinutes());
    }

    @Test
    void should_convert_time_in_total_minutes() {
        HoursMinutes object = new HoursMinutes(3, 10);
        assertEquals(190, object.convertToIntegerMinutes());
    }

    @Test
    void should_convert_to_phrasal_expression_of_time() {
        HoursMinutes object1 = new HoursMinutes(3, 10);
        assertEquals("3 hours 10 minutes", object1.convertToPhrasalValue());

        HoursMinutes object2 = new HoursMinutes(2, 0);
        assertEquals("2 hours", object2.convertToPhrasalValue());

        HoursMinutes object3 = new HoursMinutes(0, 25);
        assertEquals("25 minutes", object3.convertToPhrasalValue());

        HoursMinutes object4 = new HoursMinutes(2, 1);
        assertEquals("2 hours 1 minute", object4.convertToPhrasalValue());

        HoursMinutes object5 = new HoursMinutes(1, 34);
        assertEquals("1 hour 34 minutes", object5.convertToPhrasalValue());

        HoursMinutes object6 = new HoursMinutes(0, 0);
        assertEquals("", object6.convertToPhrasalValue());

        HoursMinutes object7 = new HoursMinutes(0, 65); // 1h 5m
        assertEquals("1 hour 5 minutes", object7.convertToPhrasalValue());
    }
}

