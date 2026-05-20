package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;

@Slf4j
@Service
public class EmailAddressFinder {

    private static final String LIST_CASE_HEARING_CENTRE_IS_NOT_PRESENT = "listCaseHearingCentre is not present";

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
            return asylumCase
                .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .map(it -> Optional.ofNullable(getEmailAddress(hearingCentreEmailAddresses, it))
                    .orElseThrow(() -> new IllegalStateException("Hearing centre email address not found: " + it.toString()))
                )
                .orElseThrow(() -> new IllegalStateException(LIST_CASE_HEARING_CENTRE_IS_NOT_PRESENT));
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

}
