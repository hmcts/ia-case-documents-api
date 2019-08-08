package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateTimeExtractor {

    public String extractHearingDate(String validIso8601HearingDate) {

        final LocalDate dateValue =
            getLocalDateTime(validIso8601HearingDate).toLocalDate();

        final String hearingDate =
            LocalDate
                .parse(dateValue.toString())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return hearingDate;
    }

    public String extractHearingTime(String validIso8601HearingDate) {

        final LocalTime timeValue =
            getLocalDateTime(validIso8601HearingDate).toLocalTime();

        final String hearingTime =
            LocalTime
                .parse(timeValue.toString())
                .format(DateTimeFormatter.ofPattern("HH:mm"));

        return hearingTime;
    }

    private LocalDateTime getLocalDateTime(String validIso8601HearingDate) {

        LocalDateTime dateTimeValue =
            LocalDateTime.parse(validIso8601HearingDate, ISO_DATE_TIME);

        return dateTimeValue;
    }
}
