package uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;



@ExtendWith(MockitoExtension.class)
public class DateUtilsTest {

    private final LocalDate exampleDateToFormat = LocalDate.parse("2023-03-03");
    private final String exampleDateTimeToFormat = "2023-06-30T00:00:00.000";
    private final String formattedExampleDate = "3 Mar 2023";
    private final String formattedExampleDateTime = "30062023";


    @Test
    void should_format_and_return_date_correctly_from_date() {
        assertEquals(formattedExampleDate, DateUtils.formatDateForNotificationAttachmentDocument(exampleDateToFormat));
    }

    @Test
    void should_format_and_return_date_correctly_from_datetime() {
        assertEquals(formattedExampleDateTime, DateUtils.formatDateTimeForNotificationAttachmentDocument(exampleDateTimeToFormat));
    }

}
