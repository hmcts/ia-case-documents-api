package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class CaseMetaData {

    private Event event;
    private String jurisdiction;
    private String caseType;
    private long caseId;
    private String paymentStatus;
    private String paymentReference;
}
