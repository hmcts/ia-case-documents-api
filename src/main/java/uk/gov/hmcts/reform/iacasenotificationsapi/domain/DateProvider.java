package uk.gov.hmcts.reform.iacasedocumentsapi.domain;

import java.time.LocalDate;

public interface DateProvider {

    LocalDate now();

    String dueDate(int plusDays);

}
