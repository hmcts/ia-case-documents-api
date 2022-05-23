package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.*;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail.helper.BailDecisionUnsignedTemplateHelper;

@Component
public class BailDecisionUnsignedMindedRefusalTemplate implements DocumentTemplate<BailCase> {

    private final String templateName;
    private final BailDecisionUnsignedTemplateHelper bailDecisionUnsignedTemplateHelper;

    public BailDecisionUnsignedMindedRefusalTemplate(
        @Value("${decisionUnsignedDocument.judgeMinded.templateName}") String templateName,
        BailDecisionUnsignedTemplateHelper bailDecisionUnsignedTemplateHelper
    ) {
        this.templateName = templateName;
        this.bailDecisionUnsignedTemplateHelper = bailDecisionUnsignedTemplateHelper;
    }

    public String getName() {
        return templateName;
    }

    @Override
    public Map<String, Object> mapFieldValues(
        CaseDetails<BailCase> caseDetails
    ) {
        final BailCase bailCase = caseDetails.getCaseData();

        final Map<String, Object> fieldValues = bailDecisionUnsignedTemplateHelper.getCommonMapFieldValues(caseDetails);

        fieldValues.put("reasonsJudgeIsMindedDetails", bailCase.read(REASONS_JUDGE_IS_MINDED_DETAILS, String.class).orElse(""));
        fieldValues.put("secretaryOfStateRefusalReasons", bailCase.read(SECRETARY_OF_STATE_REFUSAL_REASONS, String.class).orElse(""));
        fieldValues.put("reasonForRefusalDetails", bailCase.read(REASON_FOR_REFUSAL_DETAILS, String.class).orElse(""));
        fieldValues.put("tribunalRefusalReason", bailCase.read(TRIBUNAL_REFUSAL_REASON, String.class).orElse(""));

        return fieldValues;
    }
}
