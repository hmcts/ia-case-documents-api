package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.util.List;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantAddressInCountryOrOoc;

@Component
public class CmrReListingDetainedOtherAppellantLetterTemplate extends AbstractInternalCmrReListingLetterTemplate {

    public CmrReListingDetainedOtherAppellantLetterTemplate(
        @Value("${cmrReListingDetainedOtherAppellantLetter.templateName}") String templateName,
        CustomerServicesProvider customerServicesProvider,
        StringProvider stringProvider) {
        super(templateName, customerServicesProvider, stringProvider);
    }

    @Override
    protected List<String> getRecipientAddress(AsylumCase asylumCase) {
        return getAppellantAddressInCountryOrOoc(asylumCase);
    }
}
