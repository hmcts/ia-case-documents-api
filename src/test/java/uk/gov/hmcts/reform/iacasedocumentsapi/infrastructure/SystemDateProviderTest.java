package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SystemDateProviderTest {

    private final SystemDateProvider systemDateProvider = new SystemDateProvider();

    @Test
    public void returns_now_date() {
        LocalDate actualDate = systemDateProvider.now();
        assertNotNull(actualDate);
        assertFalse(actualDate.isAfter(LocalDate.now()));
    }

    @Test
    public void returns_now_date_with_time() {
        LocalDateTime actualDateTime = systemDateProvider.nowWithTime();
        assertNotNull(actualDateTime);
        assertFalse(actualDateTime.isAfter(LocalDateTime.now()));
    }

    @Test
    public void returns_now_date_plus_days_offset() {
        String actualDate = systemDateProvider.dueDate(28);
        assertNotNull(actualDate);
        assertEquals(actualDate, LocalDate.now().plusDays(28)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy")));
    }
}
