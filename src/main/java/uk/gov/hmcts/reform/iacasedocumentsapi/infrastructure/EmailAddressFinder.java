package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;

@Service
public class EmailAddressFinder {

    private final String listCaseHearingCentreIsNotPresent = "listCaseHearingCentre is not present";
    private final Map<HearingCentre, String> hearingCentreEmailAddresses;

    public EmailAddressFinder(
        Map<HearingCentre, String> hearingCentreEmailAddresses) {
        this.hearingCentreEmailAddresses = hearingCentreEmailAddresses;
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
                return emailAddressesMap.get(HearingCentre.BIRMINGHAM);
            default:
                return emailAddressesMap.get(hearingCentre);
        }
    }

    private boolean isRemoteHearing(AsylumCase asylumCase) {
        return asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class)
            .map(hearingCentre -> hearingCentre == HearingCentre.REMOTE_HEARING)
            .orElse(false);
    }

}
