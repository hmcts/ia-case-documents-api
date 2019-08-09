package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.HomeOfficePersonalisationFactory;

@Component
public class HomeOfficeCaseListedNotifier implements CaseEmailNotifier {

    private final HomeOfficePersonalisationFactory homeOfficePersonalisationFactory;
    private final Map<HearingCentre, String> homeOfficeEmailAddresses;

    public HomeOfficeCaseListedNotifier(
        HomeOfficePersonalisationFactory homeOfficePersonalisationFactory,
        Map<HearingCentre, String> homeOfficeEmailAddresses
    ) {
        requireNonNull(homeOfficePersonalisationFactory, "homeOfficePersonalisationFactory must not be null");
        requireNonNull(homeOfficeEmailAddresses, "homeOfficeEmailAddresses must not be null");

        this.homeOfficePersonalisationFactory = homeOfficePersonalisationFactory;
        this.homeOfficeEmailAddresses = homeOfficeEmailAddresses;
    }

    @Override
    public String getEmailAddress(AsylumCase asylumCase) {

        final HearingCentre listCaseHearingCentre =
            asylumCase
                .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));

        final String hearingCentreEmailAddress =
            homeOfficeEmailAddresses
                .get(listCaseHearingCentre);

        if (hearingCentreEmailAddress == null) {
            throw new IllegalStateException("Hearing centre email address not found: " + listCaseHearingCentre.toString());
        }

        return hearingCentreEmailAddress;
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {

        Map<String, String> personalisation =
            homeOfficePersonalisationFactory
                .createListedCase(asylumCase);

        return personalisation;
    }
}
