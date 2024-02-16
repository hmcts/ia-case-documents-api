package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;

@Component
public class BailSubmissionUtTemplate implements DocumentTemplate<BailCase> {

    private final String templateName;
    private final BailSubmissionTemplateProvider bailSubmissionTemplateProvider;

    public BailSubmissionUtTemplate(
        @Value("${bailSubmissionDocument.templateName}") String templateName,
        BailSubmissionTemplateProvider bailSubmissionTemplateProvider) {
        this.templateName = templateName;
        this.bailSubmissionTemplateProvider = bailSubmissionTemplateProvider;
    }

    public String getName() {
        return templateName;
    }


    @Override
    public Map<String, Object> mapFieldValues(
        CaseDetails<BailCase> caseDetails
    ) {
        return bailSubmissionTemplateProvider.mapFieldValues(caseDetails);
    }
}