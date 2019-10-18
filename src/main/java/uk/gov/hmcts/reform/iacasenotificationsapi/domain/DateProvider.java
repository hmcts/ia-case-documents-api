package uk.gov.hmcts.reform.iacasenotificationsapi.domain;

import java.time.LocalDate;

public interface DateProvider {

    LocalDate now();
    
}
