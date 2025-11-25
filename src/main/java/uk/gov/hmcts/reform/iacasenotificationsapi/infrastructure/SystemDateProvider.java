package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;

@Service
public class SystemDateProvider implements DateProvider {

    public LocalDate now() {
        return LocalDate.now();
    }

    public String dueDate(int plusDays) {
        return LocalDate.now().plusDays(plusDays)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));
    }
}
