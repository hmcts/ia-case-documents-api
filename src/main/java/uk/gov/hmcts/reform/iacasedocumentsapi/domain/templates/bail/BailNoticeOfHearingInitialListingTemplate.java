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
public class BailNoticeOfHearingInitialListingTemplate
    extends BailNoticeOfHearingTemplate implements DocumentTemplate<BailCase> {

    private final String initialListingTemplateName;

    public BailNoticeOfHearingInitialListingTemplate(
        @Value("${bailNoticeOfHearingInitialListing.templateName}") String initialListingTemplateName,
        CustomerServicesProvider customerServicesProvider,
        StringProvider stringProvider) {

        super(customerServicesProvider, stringProvider);
        this.initialListingTemplateName = initialListingTemplateName;
    }

    @Override
    public String getName() {

        return initialListingTemplateName;
    }

    @Override
    public Map<String, Object> mapFieldValues(CaseDetails<BailCase> caseDetails) {

        return super.mapFieldValues(caseDetails);
    }
}
