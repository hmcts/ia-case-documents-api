package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class DynamicListTest {

    private String testValue = "test";
    private String testLabel = "label";

    @Test
    public void should_hold_onto_values_1() {
        DynamicList dynamicList1 = new DynamicList(testValue);

        assertNull(dynamicList1.getListItems());
        assertEquals(testValue, dynamicList1.getValue().getCode());
        assertEquals(testValue, dynamicList1.getValue().getLabel());
    }

    @Test
    public void should_hold_onto_values_2() {
        List<Value> items = newArrayList(new Value(testValue, testLabel));
        DynamicList dynamicList2 = new DynamicList(new Value(testValue, testLabel), items);

        assertEquals(items, dynamicList2.getListItems());
        assertEquals(testValue, dynamicList2.getValue().getCode());
        assertEquals(testLabel, dynamicList2.getValue().getLabel());
    }
}
