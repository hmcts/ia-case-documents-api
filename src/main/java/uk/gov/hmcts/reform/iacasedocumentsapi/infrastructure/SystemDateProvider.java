package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;

@Service
public class SystemDateProvider implements DateProvider {

    public LocalDate now() {
        return LocalDate.now();
    }

    public LocalDateTime nowWithTime() {
        return LocalDateTime.now();
    }

    public String dueDate(int plusDays) {
        return LocalDate.now().plusDays(plusDays)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));
    }

    public String plusWeeks(int plusWeeks) {
        return LocalDate.now().plusWeeks(plusWeeks)
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));
    }
}
