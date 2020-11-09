package uk.gov.hmcts.reform.iacasedocumentsapi.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FileTypeTest {

    @Test
    void has_correct_values() {
        assertEquals("PDF", FileType.PDF.toString());
        assertEquals("pdf", FileType.PDF.getValue());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(1, FileType.values().length);
    }

    @Test
    void throws_when_trying_to_create_from_invalid_value() {

        assertThatThrownBy(() -> FileType.from("invalid-value"))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("invalid-value not a FileType");
    }

    @Test
    void throws_when_trying_to_create_from_invalid_value2() {

        assertEquals(FileType.PDF, FileType.from("pdf"));
    }
}
