package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;

@Service
public class EmailAddressFinder {

    private final Map<HearingCentre, String> hearingCentreEmailAddresses;
    private final Map<HearingCentre, String> homeOfficeEmailAddresses;

    public EmailAddressFinder(
        Map<HearingCentre, String> hearingCentreEmailAddresses,
        Map<HearingCentre, String> homeOfficeEmailAddresses) {
        this.hearingCentreEmailAddresses = hearingCentreEmailAddresses;
        this.homeOfficeEmailAddresses = homeOfficeEmailAddresses;
    }

    public String getEmailAddress(AsylumCase asylumCase) {

        final HearingCentre hearingCentre =
            getHearingCentre(asylumCase, AsylumCaseDefinition.HEARING_CENTRE);

        final String hearingCentreEmailAddress =
            hearingCentreEmailAddresses
                .get(hearingCentre);

        if (hearingCentreEmailAddress == null) {
            throw new IllegalStateException("Hearing centre email address not found: " + hearingCentre.toString());
        }

        return hearingCentreEmailAddress;
    }

    public String getLegalRepEmailAddress(AsylumCase asylumCase) {
        return asylumCase
            .read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)
            .orElseThrow(() -> new IllegalStateException("legalRepresentativeEmailAddress is not present"));
    }

    public String getHomeOfficeEmailAddress(AsylumCase asylumCase) {
        final HearingCentre listCaseHearingCentre =
            getHearingCentre(asylumCase, AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE);

        final String homeOfficeEmailAddress =
            homeOfficeEmailAddresses
                .get(listCaseHearingCentre);

        if (homeOfficeEmailAddress == null) {
            throw new IllegalStateException("List case hearing centre email address not found: " + listCaseHearingCentre.toString());
        }

        return homeOfficeEmailAddress;
    }

    public String getListCaseHearingCentreEmailAddress(AsylumCase asylumCase) {
        return asylumCase
            .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
            .map(it -> Optional.ofNullable(hearingCentreEmailAddresses.get(it))
                .orElseThrow(() -> new IllegalStateException("Hearing centre email address not found: " + it.toString()))
            )
            .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));

    }

    private HearingCentre getHearingCentre(AsylumCase asylumCase, AsylumCaseDefinition asylumCaseDefinition) {
        return asylumCase
            .read(asylumCaseDefinition, HearingCentre.class)
            .orElseThrow(() -> new IllegalStateException("hearingCentre is not present"));
    }
}
