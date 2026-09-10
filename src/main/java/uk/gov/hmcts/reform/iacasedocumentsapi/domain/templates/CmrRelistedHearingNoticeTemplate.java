package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.CmrRelistedHearingNoticeFieldMapper;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;

import java.util.Map;

@Component
public class CmrRelistedHearingNoticeTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final StringProvider stringProvider;
    private final CustomerServicesProvider customerServicesProvider;
    private final HearingDetailsFinder hearingDetailsFinder;

    public CmrRelistedHearingNoticeTemplate(
            @Value("${cmrRelistedHearingNoticeDocument.templateName}") String templateName,
            StringProvider stringProvider,
            CustomerServicesProvider customerServicesProvider,
            HearingDetailsFinder hearingDetailsFinder) {
        this.templateName = templateName;
        this.stringProvider = stringProvider;
        this.customerServicesProvider = customerServicesProvider;
        this.hearingDetailsFinder = hearingDetailsFinder;
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
                        customerServicesProvider,
                        hearingDetailsFinder
                );

        return fieldMapper.mapFieldValues(caseDetails, caseDetailsBefore);
    }
}