package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;

@Service
@Slf4j
public class SystemDateProvider implements DateProvider {

    public LocalDate now() {
        log.info("-----------LocalDate.now() {}", LocalDate.now());
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
