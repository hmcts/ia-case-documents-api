package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.format.DateTimeParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class DateTimeExtractorTest {

    final String listCaseHearingDate = "2019-05-03T14:25:15.000";
    final String invalidIso8601HearingDate = "2019-05-03 14:25:15";
    final String extractedHearingDateFormatted = "3 May 2019";
    final String extractedHearingTime = "14:25";
    @Mock
    private DateTimeExtractor dateTimeExtractor;

    @BeforeEach
    public void setUp() {
        dateTimeExtractor = new DateTimeExtractor();
    }

    @Test
    public void should_throw_when_invalid_iso_8610_date() {

        assertThatThrownBy(() -> dateTimeExtractor.extractHearingDate(invalidIso8601HearingDate))
            .isExactlyInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void should_throw_when_invalid_iso_8610_time() {

        assertThatThrownBy(() -> dateTimeExtractor.extractHearingTime(invalidIso8601HearingDate))
            .isExactlyInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void should_return_extracted_date_for_valid_iso_8610_datetime() {

        String actualExtractedDate = dateTimeExtractor.extractHearingDate(listCaseHearingDate);

        assertEquals(extractedHearingDateFormatted, actualExtractedDate);
    }

    @Test
    public void should_return_extracted_time_for_valid_iso_8610_datetime() {

        String actualExtractedTime = dateTimeExtractor.extractHearingTime(listCaseHearingDate);

        assertEquals(extractedHearingTime, actualExtractedTime);
    }
}
