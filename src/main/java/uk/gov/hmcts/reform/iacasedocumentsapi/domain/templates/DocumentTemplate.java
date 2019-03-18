package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import java.util.Map;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;

public interface DocumentTemplate<T extends CaseData> {

    String getName();

    Map<String, Object> mapFieldValues(
        CaseDetails<T> caseDetails
    );
}
