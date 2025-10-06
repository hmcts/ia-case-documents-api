package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
public class DateTimeExtractor {

    public String extractHearingDate(String validIso8601HearingDate) {

        final LocalDate dateValue =
            getLocalDateTime(validIso8601HearingDate).toLocalDate();

        return LocalDate
                .parse(dateValue.toString())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));
    }

    public String extractHearingTime(String validIso8601HearingDate) {

        final LocalTime timeValue =
            getLocalDateTime(validIso8601HearingDate).toLocalTime();

        return LocalTime
                .parse(timeValue.toString())
                .format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private LocalDateTime getLocalDateTime(String validIso8601HearingDate) {

        return LocalDateTime.parse(validIso8601HearingDate, ISO_DATE_TIME);
    }
}
