package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;


@Component
public class HearingNoticeUpdatedRequirementsTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final StringProvider stringProvider;
    private final HearingNoticeUpdatedTemplateProvider hearingNoticeUpdatedTemplateProvider;

    public HearingNoticeUpdatedRequirementsTemplate(
        @Value("${hearingNoticeUpdatedRequirementsDocument.templateName}") String templateName,
        StringProvider stringProvider,
        HearingNoticeUpdatedTemplateProvider hearingNoticeUpdatedTemplateProvider
    ) {
        this.templateName = templateName;
        this.stringProvider = stringProvider;
        this.hearingNoticeUpdatedTemplateProvider = hearingNoticeUpdatedTemplateProvider;
    }

    public String getName() {
        return templateName;
    }

    @Override
    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails,
        CaseDetails<AsylumCase> caseDetailsBefore
    ) {
        return hearingNoticeUpdatedTemplateProvider.mapFieldValues(caseDetails, caseDetailsBefore);
    }
}
