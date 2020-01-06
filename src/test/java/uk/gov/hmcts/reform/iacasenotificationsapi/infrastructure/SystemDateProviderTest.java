package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.Test;

public class SystemDateProviderTest {

    private final SystemDateProvider systemDateProvider = new SystemDateProvider();

    @Test
    public void returns_now_date() {
        LocalDate actualDate = systemDateProvider.now();
        assertNotNull(actualDate);
        assertFalse(actualDate.isAfter(LocalDate.now()));
    }

    @Test
    public void returns_now_date_plus_days_offset() {
        String actualDate = systemDateProvider.dueDate(14);
        assertNotNull(actualDate);
        assertEquals(actualDate, LocalDate.now().plusDays(14)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy")));
    }
}
