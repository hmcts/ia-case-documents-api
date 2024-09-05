package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;

@Component
public class BailNoticeOfHearingConditionalBailRelistingTemplate
    extends BailNoticeOfHearingTemplate implements DocumentTemplate<BailCase> {

    private final String conditionalBailRelistingTemplateName;

    public BailNoticeOfHearingConditionalBailRelistingTemplate(
        @Value("${bailNoticeOfHearingConditionalBailRelisting.templateName}") String conditionalBailRelistingTemplateName,
        CustomerServicesProvider customerServicesProvider,
        StringProvider stringProvider) {

        super(customerServicesProvider, stringProvider);
        this.conditionalBailRelistingTemplateName = conditionalBailRelistingTemplateName;
    }

    @Override
    public String getName() {

        return conditionalBailRelistingTemplateName;
    }

    @Override
    public Map<String, Object> mapFieldValues(CaseDetails<BailCase> caseDetails) {

        return super.mapFieldValues(caseDetails);
    }
}
