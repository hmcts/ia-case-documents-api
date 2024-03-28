package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.helper.UpdatedTribunalDecisionAndReasonsCoverLetterTemplateHelper;

@Component
public class UpdatedTribunalDecisionAndReasonsCoverLetterTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final UpdatedTribunalDecisionAndReasonsCoverLetterTemplateHelper templateHelper;

    public UpdatedTribunalDecisionAndReasonsCoverLetterTemplate(
        @Value("${decisionAndReasonsCoverLetter.templateName}") String templateName,
        UpdatedTribunalDecisionAndReasonsCoverLetterTemplateHelper templateHelper
    ) {
        this.templateName = templateName;
        this.templateHelper = templateHelper;
    }

    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(CaseDetails<AsylumCase> caseDetails) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final Map<String, Object> fieldValues = templateHelper.getCommonMapFieldValues(caseDetails);

        fieldValues.put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)
                .orElse(""));

        return fieldValues;
    }
}
