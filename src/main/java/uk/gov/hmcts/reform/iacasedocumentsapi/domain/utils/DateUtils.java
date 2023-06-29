package uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils;

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private DateUtils() {
        // prevent public constructor for Sonar
    }

    // Formatting to be used for dates within notification attachment documents
    public static String formatDateForNotificationAttachmentDocument(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ofPattern("d MMM yyyy"));
    }

    // Formatting to be used for DateTimes to date, e.g. 3 June 2023
    public static String formatDateTimeForNotificationAttachmentDocument(String date) {
        if (!Strings.isNullOrEmpty(date)) {
            return LocalDateTime.parse(date).format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        }
        return "";
    }
}
