package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;

@Slf4j
@Service
public class EmailAddressFinder {

    private static final Set<HearingCentre> SHARED_HEARING_CENTRES = Set.of(HearingCentre.HENDON,
            HearingCentre.BRADFORD_KEIGHLEY, HearingCentre.MCC_MINSHULL, HearingCentre.MCC_CROWN_SQUARE,
            HearingCentre.MANCHESTER_MAGS, HearingCentre.NTH_TYNE_MAGS, HearingCentre.LEEDS_MAGS, HearingCentre.ALLOA_SHERRIF);

    private static final String NO_EMAIL_ADDRESS_DECISION_WITHOUT_HEARING = "No email address for decisions made without hearing";
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

        log.info("------------Hearing centre email address is {}", hearingCentreEmailAddress);
        if (hearingCentreEmailAddress == null) {
            throw new IllegalStateException("Hearing centre email address not found: " + hearingCentre.toString());
        }

        return hearingCentreEmailAddress;
    }

    public String getListCaseHearingCentreEmailAddress(AsylumCase asylumCase) {
        if (isRemoteHearing(asylumCase)) {
            return getHearingCentreEmailAddress(asylumCase);
        } else {
            HearingCentre hearingCentre = asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                    .orElseThrow(() -> new IllegalStateException(LIST_CASE_HEARING_CENTRE.value() + " is not present"));

            if (isASharedHearingCentre(hearingCentre)) {
                hearingCentre = asylumCase.read(HEARING_CENTRE, HearingCentre.class)
                        .orElseThrow(() -> new IllegalStateException(HEARING_CENTRE.value() + " is not present"));
            }

            String emailAddress = getEmailAddress(hearingCentreEmailAddresses, hearingCentre);
            if (emailAddress == null) {
                throw new IllegalStateException("List case hearing centre email address not found: " + hearingCentre.getValue());
            }
            return emailAddress;
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
            case DECISION_WITHOUT_HEARING:
                return NO_EMAIL_ADDRESS_DECISION_WITHOUT_HEARING;
            default:
                return emailAddressesMap.get(hearingCentre);
        }
    }

    private boolean isRemoteHearing(AsylumCase asylumCase) {
        return asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class)
            .map(hearingCentre -> hearingCentre == HearingCentre.REMOTE_HEARING)
            .orElse(false);
    }

    private static boolean isASharedHearingCentre(HearingCentre hearingCentre) {
        return SHARED_HEARING_CENTRES.contains(hearingCentre);
    }

}
