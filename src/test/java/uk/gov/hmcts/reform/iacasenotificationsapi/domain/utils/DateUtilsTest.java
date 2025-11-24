package uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;



@ExtendWith(MockitoExtension.class)
public class DateUtilsTest {

    private final LocalDate exampleDateToFormat = LocalDate.parse("2023-03-03");
    private final String formattedExampleDate = "3 Mar 2023";

    @Test
    void should_format_and_return_date_correctly_from_date() {
        assertEquals(formattedExampleDate, DateUtils.formatDateForNotification(exampleDateToFormat));
    }

}