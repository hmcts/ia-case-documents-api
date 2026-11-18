package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.HearingNoticeFieldMapper;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;

@Component
public class Stf24WeeksHearingNoticeTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final StringProvider stringProvider;
    private final CustomerServicesProvider customerServicesProvider;

    public Stf24WeeksHearingNoticeTemplate(
            @Value("${stf24WeeksHearingNoticeDocument.templateName}") String templateName,
            StringProvider stringProvider,
            CustomerServicesProvider customerServicesProvider
    ) {
        this.templateName = templateName;
        this.stringProvider = stringProvider;
        this.customerServicesProvider = customerServicesProvider;
    }

    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
            CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase =
            caseDetails.getCaseData();

        final HearingNoticeFieldMapper fieldMapper
            = new HearingNoticeFieldMapper(stringProvider, customerServicesProvider);

        return fieldMapper.mapFields(asylumCase);
    }
}
