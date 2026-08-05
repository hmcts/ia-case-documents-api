package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_REMOVAL_OF_24W_APPLICATION_REFUSED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.REMOVAL_OF_24W_DECISION_JUDGE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.REMOVAL_OF_24W_DECISION_REASON;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.getAppellantFullName;

@Component
public class Stf24WeeksRemovalDecisionTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;

    public Stf24WeeksRemovalDecisionTemplate(
        @Value("${stf24WeeksRemovalDecision.templateName}") String templateName
    ) {
        this.templateName = templateName;
    }

    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();
        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("appellantFullName", getAppellantFullName(asylumCase));
        fieldValues.put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("legalRepRefTitle", "Legal representative reference");
        fieldValues.put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""));

        if (asylumCase.read(IS_REMOVAL_OF_24W_APPLICATION_REFUSED, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            fieldValues.put("suitability", "suitable");
            fieldValues.put("postAmble", "Therefore this appeal will continue to be processed under the accelerated"
                + " statutory 24 week timeline.");
        } else {
            fieldValues.put("suitability", "unsuitable");
            fieldValues.put("postAmble", "Therefore this appeal will no longer be processed under the accelerated"
                + " statutory 24 week timeline and will be processed under the standard appeal process.");
        }
        fieldValues.put("suitabilityReason", asylumCase.read(REMOVAL_OF_24W_DECISION_REASON, String.class).orElse(""));
        fieldValues.put("judgeName", asylumCase.read(REMOVAL_OF_24W_DECISION_JUDGE, String.class).orElse(""));
        fieldValues.put("decisionDate", LocalDate.now().toString());
        return fieldValues;
    }
}
