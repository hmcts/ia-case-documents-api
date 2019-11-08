package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

import java.util.Map;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
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
            asylumCase
                .read(HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("hearingCentre is not present"));

        final String hearingCentreEmailAddress =
            hearingCentreEmailAddresses
                .get(hearingCentre);

        if (hearingCentreEmailAddress == null) {
            throw new IllegalStateException("Hearing centre email address not found: " + hearingCentre.toString());
        }

        return hearingCentreEmailAddress;
    }

    public String getHomeOfficeEmailAddress(AsylumCase asylumCase) {
        final HearingCentre listCaseHearingCentre =
                asylumCase
                        .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                        .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));

        final String homeOfficeEmailAddress =
                homeOfficeEmailAddresses
                        .get(listCaseHearingCentre);

        if (homeOfficeEmailAddress == null) {
            throw new IllegalStateException("List case hearing centre email address not found: " + listCaseHearingCentre.toString());
        }

        return homeOfficeEmailAddress;
    }
}
