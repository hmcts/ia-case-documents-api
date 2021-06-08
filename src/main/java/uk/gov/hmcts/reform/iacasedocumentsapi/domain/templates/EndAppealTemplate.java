package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.helper.EndAppealTemplateHelper;

@Component
public class EndAppealTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final EndAppealTemplateHelper endAppealTemplateHelper;

    public EndAppealTemplate(
        @Value("${endAppeal.templateName}") String templateName,
        EndAppealTemplateHelper endAppealTemplateHelper
    ) {
        this.templateName = templateName;
        this.endAppealTemplateHelper = endAppealTemplateHelper;
    }

    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        Map<String, Object> fieldValues = endAppealTemplateHelper.getCommonMapFieldValues(caseDetails);
        fieldValues.put("legalRepReferenceNumber", caseDetails.getCaseData().read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""));
        return fieldValues;
    }

}
