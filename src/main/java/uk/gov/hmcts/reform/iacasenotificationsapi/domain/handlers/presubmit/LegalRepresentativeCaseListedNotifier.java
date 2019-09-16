package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;

import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LegalRepresentativePersonalisationFactory;

@Component
public class LegalRepresentativeCaseListedNotifier implements CaseEmailNotifier {

    private final LegalRepresentativePersonalisationFactory legalRepresentativePersonalisationFactory;

    public LegalRepresentativeCaseListedNotifier(
        LegalRepresentativePersonalisationFactory legalRepresentativePersonalisationFactory
    ) {
        requireNonNull(legalRepresentativePersonalisationFactory, "legalRepresentativePersonalisationFactory must not be null");
        this.legalRepresentativePersonalisationFactory = legalRepresentativePersonalisationFactory;
    }

    @Override
    public String getEmailAddress(AsylumCase asylumCase) {

        return asylumCase
            .read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)
            .orElseThrow(() -> new IllegalStateException("legalRepresentativeEmailAddress is not present"));

    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {

        return legalRepresentativePersonalisationFactory.createListedCase(asylumCase);

    }
}
