package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.CmrHearingNoticeFieldMapper;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;

public class CmrHearingNoticeTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final StringProvider stringProvider;
    private final CustomerServicesProvider customerServicesProvider;

    public CmrHearingNoticeTemplate(String templateName, StringProvider stringProvider,
                                    CustomerServicesProvider customerServicesProvider) {
        this.templateName = templateName;
        this.stringProvider = stringProvider;
        this.customerServicesProvider = customerServicesProvider;
    }

    public String getName() {
        return templateName;
    }

    @Override
    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase =
            caseDetails.getCaseData();

        final CmrHearingNoticeFieldMapper fieldMapper =
            new CmrHearingNoticeFieldMapper(stringProvider, customerServicesProvider);

        return fieldMapper.mapFields(asylumCase);
    }
}
