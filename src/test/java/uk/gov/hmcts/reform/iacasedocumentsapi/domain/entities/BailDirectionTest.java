package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

public class BailDirectionTest {

    private static String TEST_VALUE = "some-value";
    private static List<IdValue<PreviousDates>> TEST_VALUE_PD = Collections.emptyList();

    private BailDirection bailDirection = new BailDirection(
        TEST_VALUE,
        TEST_VALUE,
        TEST_VALUE,
        TEST_VALUE,
        TEST_VALUE,
        TEST_VALUE,
        TEST_VALUE_PD
    );

    @Test
    public void should_hold_onto_values() {

        assertEquals(TEST_VALUE, bailDirection.getSendDirectionList());
        assertEquals(TEST_VALUE, bailDirection.getSendDirectionDescription());
        assertEquals(TEST_VALUE, bailDirection.getDateOfCompliance());
        assertEquals(TEST_VALUE, bailDirection.getDateSent());
        assertEquals(TEST_VALUE, bailDirection.getDateTimeDirectionCreated());
        assertEquals(TEST_VALUE, bailDirection.getDateTimeDirectionModified());
        assertEquals(TEST_VALUE_PD, bailDirection.getPreviousDates());

    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new BailDirection(null, null, null, null, null, null, null))
            .isExactlyInstanceOf(NullPointerException.class);

    }
}
