package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.LetterNotificationPersonalisation;


import java.util.Set;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getLegalRepAddressInCountryOrOocAsSet;

public interface LegalRepresentativeLetterNotificationPersonalisation extends LetterNotificationPersonalisation {

    @Override
    default Set<String> getRecipientsList(AsylumCase asylumCase) {
        return getLegalRepAddressInCountryOrOocAsSet(asylumCase);
    }
}
