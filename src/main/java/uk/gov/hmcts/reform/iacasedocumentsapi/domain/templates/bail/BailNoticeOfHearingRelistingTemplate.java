package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class BailNoticeOfHearingRelistingTemplate
    extends BailNoticeOfHearingTemplate implements DocumentTemplate<BailCase> {

    private final String relistingTemplateName;

    public BailNoticeOfHearingRelistingTemplate(
        @Value("${bailNoticeOfHearingRelisting.templateName}") String relistingTemplateName,
        CustomerServicesProvider customerServicesProvider,
        StringProvider stringProvider) {

        super(customerServicesProvider, stringProvider);
        this.relistingTemplateName = relistingTemplateName;
    }

    @Override
    public String getName() {

        return relistingTemplateName;
    }

    @Override
    public Map<String, Object> mapFieldValues(CaseDetails<BailCase> caseDetails) {

        return super.mapFieldValues(caseDetails);
    }
}

