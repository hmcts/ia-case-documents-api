package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.CmrRelistedHearingNoticeFieldMapper;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;

public class CmrRelistedHearingNoticeTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final StringProvider stringProvider;
    private final CustomerServicesProvider customerServicesProvider;

    public CmrRelistedHearingNoticeTemplate(
            String templateName,
            StringProvider stringProvider,
            CustomerServicesProvider customerServicesProvider) {
        this.templateName = templateName;
        this.stringProvider = stringProvider;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getName() {
        return templateName;
    }

    @Override
    public Map<String, Object> mapFieldValues(
            CaseDetails<AsylumCase> caseDetails
    ) {
        return mapFieldValues(caseDetails, caseDetails);
    }

    public Map<String, Object> mapFieldValues(
            CaseDetails<AsylumCase> caseDetails,
            CaseDetails<AsylumCase> caseDetailsBefore
    ) {
        final CmrRelistedHearingNoticeFieldMapper fieldMapper =
                new CmrRelistedHearingNoticeFieldMapper(
                        stringProvider,
                        customerServicesProvider
                );

        return fieldMapper.mapFieldValues(caseDetails, caseDetailsBefore);
    }
}