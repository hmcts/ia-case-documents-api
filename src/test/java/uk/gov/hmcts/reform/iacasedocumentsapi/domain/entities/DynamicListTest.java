package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DynamicListTest {

    @Mock
    private Value value;

    @Test
    void should_initialize_with_value_and_list_of_values() {
        List<Value> lov = List.of(value,value,value);
        DynamicList dynamicList = new DynamicList(value, lov);

        assertEquals(value, dynamicList.getValue());
        assertEquals(lov, dynamicList.getListItems());
    }

    @Test
    void should_set_fields() {
        DynamicList dynamicList = new DynamicList(null, Collections.emptyList());
        dynamicList.setValue(value);

        assertEquals(value, dynamicList.getValue());
    }
}
