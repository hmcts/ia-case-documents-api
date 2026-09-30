package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getLegalRepAddressInCountryOrOoc;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getLegalRepPersonalisation;

@Component
public class InternalCmrReListingLrLetterTemplate extends AbstractInternalCmrReListingLetterTemplate {

    public InternalCmrReListingLrLetterTemplate(
        @Value("${internalCmrReListingLrLetter.templateName}") String templateName,
        CustomerServicesProvider customerServicesProvider,
        StringProvider stringProvider) {
        super(templateName, customerServicesProvider, stringProvider);
    }

    @Override
    protected List<String> getRecipientAddress(AsylumCase asylumCase) {
        return getLegalRepAddressInCountryOrOoc(asylumCase);
    }

    @Override
    protected Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return getLegalRepPersonalisation(asylumCase);
    }
}
