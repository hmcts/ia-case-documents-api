package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    public void should_return_date_plus_positive_weeks_in_correct_format() {
        // Given
        int weeksToAdd = 2;
        LocalDate expectedDate = LocalDate.now().plusWeeks(weeksToAdd);
        String expectedFormattedDate = expectedDate.format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        // When
        String actualFormattedDate = systemDateProvider.plusWeeks(weeksToAdd);

        // Then
        assertEquals(expectedFormattedDate, actualFormattedDate);
    }

    @Test
    public void should_return_current_date_when_zero_weeks_added() {
        // Given
        int weeksToAdd = 0;
        LocalDate expectedDate = LocalDate.now();
        String expectedFormattedDate = expectedDate.format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        // When
        String actualFormattedDate = systemDateProvider.plusWeeks(weeksToAdd);

        // Then
        assertEquals(expectedFormattedDate, actualFormattedDate);
    }

    @Test
    public void should_return_date_minus_weeks_when_negative_weeks_provided() {
        // Given
        int weeksToSubtract = -3;
        LocalDate expectedDate = LocalDate.now().plusWeeks(weeksToSubtract); // plusWeeks handles negative values
        String expectedFormattedDate = expectedDate.format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        // When
        String actualFormattedDate = systemDateProvider.plusWeeks(weeksToSubtract);

        // Then
        assertEquals(expectedFormattedDate, actualFormattedDate);
    }

    @Test
    public void should_return_date_in_correct_format_with_single_digit_day() {
        // Given
        int weeksToAdd = 1;
        String actualResult = systemDateProvider.plusWeeks(weeksToAdd);

        // Then - Verify the format matches the expected pattern "d MMM yyyy"
        // The format should have single digit day (no leading zero), abbreviated month, and 4-digit year
        assertNotNull(actualResult);
        assertTrue(actualResult.matches("\\d{1,2} [A-Za-z]{3,4} \\d{4}"), 
                   "Date format should match 'd MMM yyyy' pattern, but was: " + actualResult);
    }

    @Test
    public void should_handle_large_positive_week_values() {
        // Given
        int weeksToAdd = 52; // One year
        LocalDate expectedDate = LocalDate.now().plusWeeks(weeksToAdd);
        String expectedFormattedDate = expectedDate.format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        // When
        String actualFormattedDate = systemDateProvider.plusWeeks(weeksToAdd);

        // Then
        assertEquals(expectedFormattedDate, actualFormattedDate);
    }

    @Test
    public void should_handle_large_negative_week_values() {
        // Given
        int weeksToSubtract = -26; // Half a year ago
        LocalDate expectedDate = LocalDate.now().plusWeeks(weeksToSubtract);
        String expectedFormattedDate = expectedDate.format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        // When
        String actualFormattedDate = systemDateProvider.plusWeeks(weeksToSubtract);

        // Then
        assertEquals(expectedFormattedDate, actualFormattedDate);
    }
}
