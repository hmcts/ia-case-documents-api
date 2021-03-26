package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;
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

    private String listCaseHearingCentreIsNotPresent = "listCaseHearingCentre is not present";
    private final Map<HearingCentre, String> hearingCentreEmailAddresses;
    private final Map<HearingCentre, String> homeOfficeEmailAddresses;
    private final Map<HearingCentre, String> homeOfficeFtpaEmailAddresses;

    public EmailAddressFinder(
        Map<HearingCentre, String> hearingCentreEmailAddresses,
        Map<HearingCentre, String> homeOfficeEmailAddresses,
        Map<HearingCentre, String> homeOfficeFtpaEmailAddresses) {
        this.hearingCentreEmailAddresses = hearingCentreEmailAddresses;
        this.homeOfficeEmailAddresses = homeOfficeEmailAddresses;
        this.homeOfficeFtpaEmailAddresses = homeOfficeFtpaEmailAddresses;
    }

    public String getHearingCentreEmailAddress(AsylumCase asylumCase) {

        final HearingCentre hearingCentre =
            getHearingCentre(asylumCase, HEARING_CENTRE);

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

    public String getListCaseHomeOfficeEmailAddress(AsylumCase asylumCase) {
        if (isRemoteHearing(asylumCase)) {
            return getHearingCentreEmailAddress(asylumCase);
        } else {
            return asylumCase
                .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .map(it -> Optional.ofNullable(getEmailAddress(homeOfficeEmailAddresses, it))
                    .orElseThrow(() -> new IllegalStateException("List case hearing centre email address not found: " + it.toString()))
                )
                .orElseThrow(() -> new IllegalStateException(listCaseHearingCentreIsNotPresent));
        }

    }

    public String getListCaseFtpaHomeOfficeEmailAddress(AsylumCase asylumCase) {
        if (isRemoteHearing(asylumCase)) {
            return getHearingCentreEmailAddress(asylumCase);
        } else {
            return asylumCase
                .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .map(it -> Optional.ofNullable(getEmailAddress(homeOfficeFtpaEmailAddresses, it))
                    .orElseThrow(() -> new IllegalStateException("List case hearing centre ftpa email address not found: " + it.toString()))
                )
                .orElseThrow(() -> new IllegalStateException(listCaseHearingCentreIsNotPresent));
        }
    }

    public String getListCaseHearingCentreEmailAddress(AsylumCase asylumCase) {
        if (isRemoteHearing(asylumCase)) {
            return getHearingCentreEmailAddress(asylumCase);
        } else {
            return asylumCase
                .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .map(it -> Optional.ofNullable(getEmailAddress(hearingCentreEmailAddresses, it))
                    .orElseThrow(() -> new IllegalStateException("Hearing centre email address not found: " + it.toString()))
                )
                .orElseThrow(() -> new IllegalStateException(listCaseHearingCentreIsNotPresent));
        }
    }

    private HearingCentre getHearingCentre(AsylumCase asylumCase, AsylumCaseDefinition asylumCaseDefinition) {
        return asylumCase
            .read(asylumCaseDefinition, HearingCentre.class)
            .orElseThrow(() -> new IllegalStateException("hearingCentre is not present"));
    }

    private String getEmailAddress(Map<HearingCentre, String> emailAddressesMap, HearingCentre hearingCentre) {
        switch (hearingCentre) {
            case GLASGOW_TRIBUNAL_CENTRE:
                return emailAddressesMap.get(HearingCentre.GLASGOW);
            case NOTTINGHAM:
            case COVENTRY:
                return emailAddressesMap.get(HearingCentre.BRADFORD);
            default:
                return emailAddressesMap.get(hearingCentre);
        }
    }

    public String getHomeOfficeEmailAddress(AsylumCase asylumCase) {
        return asylumCase
                .read(HEARING_CENTRE, HearingCentre.class)
                .map(it -> Optional.ofNullable(getEmailAddress(homeOfficeEmailAddresses, it))
                        .orElseThrow(() -> new IllegalStateException("Hearing centre email address not found: " + it.toString()))
                )
                .orElseThrow(() -> new IllegalStateException("hearingCentre is not present"));
    }

    private boolean isRemoteHearing(AsylumCase asylumCase) {
        return asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class)
            .map(hearingCentre -> hearingCentre == HearingCentre.REMOTE_HEARING)
            .orElse(false);
    }

}
