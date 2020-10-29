package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

public class SystemDateProviderTest {

    private final SystemDateProvider systemDateProvider = new SystemDateProvider();

    @Test
    public void returns_now_date() {
        LocalDate actualDate = systemDateProvider.now();
        assertNotNull(actualDate);
        assertFalse(actualDate.isAfter(LocalDate.now()));
    }
}
