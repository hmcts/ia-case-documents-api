package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;

@Service
public class SystemDateProvider implements DateProvider {

    public LocalDate now() {
        return LocalDate.now();
    }
}
