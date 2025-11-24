package uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private DateUtils() {
        // prevent public constructor for Sonar
    }

    // Formatting to be used for dates within notification attachment documents
    public static String formatDateForNotification(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ofPattern("d MMM yyyy"));
    }
}
