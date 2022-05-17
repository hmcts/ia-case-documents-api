package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail.helper.BailEndApplicationTemplateHelper;

@Component
public class BailEndApplicationTemplate implements DocumentTemplate<BailCase> {

    private final String templateName;
    private final BailEndApplicationTemplateHelper bailEndApplicationTemplateHelper;

    public BailEndApplicationTemplate(
        @Value("${bailEndApplication.templateName}") String templateName,
        BailEndApplicationTemplateHelper bailEndApplicationTemplateHelper) {
        this.templateName = templateName;
        this.bailEndApplicationTemplateHelper = bailEndApplicationTemplateHelper;
    }

    public String getName() {
        return templateName;
    }

    @Override
    public Map<String, Object> mapFieldValues(
        CaseDetails<BailCase> caseDetails
    ) {
        final BailCase bailCase = caseDetails.getCaseData();

        final Map<String, Object> fieldValues = bailEndApplicationTemplateHelper.getCommonMapFieldValues(caseDetails);

        return fieldValues;
    }

}
