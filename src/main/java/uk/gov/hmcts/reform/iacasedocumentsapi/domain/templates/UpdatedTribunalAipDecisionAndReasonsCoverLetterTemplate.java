package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.helper.UpdatedTribunalDecisionAndReasonsCoverLetterTemplateHelper;

@Component
public class UpdatedTribunalAipDecisionAndReasonsCoverLetterTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final UpdatedTribunalDecisionAndReasonsCoverLetterTemplateHelper templateHelper;

    public UpdatedTribunalAipDecisionAndReasonsCoverLetterTemplate(
        @Value("${aipDecisionAndReasonsCoverLetter.templateName}") String templateName,
        UpdatedTribunalDecisionAndReasonsCoverLetterTemplateHelper templateHelper
    ) {
        this.templateName = templateName;
        this.templateHelper = templateHelper;
    }

    public String getName() {
        return templateName;
    }

    @Override
    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        return templateHelper.getCommonMapFieldValues(caseDetails);
    }
}
