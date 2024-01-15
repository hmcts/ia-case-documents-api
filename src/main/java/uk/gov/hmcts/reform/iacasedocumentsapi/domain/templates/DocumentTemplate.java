package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import java.util.Collections;
import java.util.Map;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;

public interface DocumentTemplate<T extends CaseData> {

    default String getName() {
        return null;
    }

    default String getName(CaseDetails<T> caseDetails ) {
        return getName();
    }

    default Map<String, Object> mapFieldValues(
        CaseDetails<T> caseDetails
    ) {
        return Collections.emptyMap();
    }

    default Map<String, Object> mapFieldValues(
        CaseDetails<T> caseDetails,
        CaseDetails<T> caseDetailsBefore
    ) {
        return mapFieldValues(caseDetails);
    }
}
