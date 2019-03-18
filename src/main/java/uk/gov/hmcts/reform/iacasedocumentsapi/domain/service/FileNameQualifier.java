package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;

public interface FileNameQualifier<T extends CaseData> {

    String get(
        String unqualifiedFileName,
        CaseDetails<T> caseDetails
    );
}
