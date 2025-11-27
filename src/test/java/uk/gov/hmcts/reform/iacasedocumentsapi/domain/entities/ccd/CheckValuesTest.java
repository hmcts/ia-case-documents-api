package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

public class CheckValuesTest {
    private final List<String> values = List.of("Something");
    private final CheckValues<String> underTest = new CheckValues<>(values);

    @Test
    public void should_hold_onto_values() {
        assertEquals(values, underTest.getValues());
    }
}
