package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;

public class TemplateUtils {

    private TemplateUtils() {

    }

    public static AsylumCase getCaseData(CaseDetails<AsylumCase> asylumCase) {
        return asylumCase.getCaseData();
    }
}
