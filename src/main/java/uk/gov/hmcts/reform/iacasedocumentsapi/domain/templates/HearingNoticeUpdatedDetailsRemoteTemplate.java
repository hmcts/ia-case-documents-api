package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;

@Component
public class HearingNoticeUpdatedDetailsRemoteTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final HearingNoticeUpdatedTemplateProvider hearingNoticeUpdatedTemplateProvider;

    public HearingNoticeUpdatedDetailsRemoteTemplate(
        @Value("${remoteHearingNoticeUpdatedDetailsDocument.templateName}") String templateName,
        HearingNoticeUpdatedTemplateProvider hearingNoticeUpdatedTemplateProvider
    ) {
        this.templateName = templateName;
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
