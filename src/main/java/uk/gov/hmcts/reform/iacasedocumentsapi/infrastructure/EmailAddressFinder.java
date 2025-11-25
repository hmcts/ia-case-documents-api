package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getLegalRepEmailInternalOrLegalRepJourney;

import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailHearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils;


@Service
public class EmailAddressFinder {

    public static final String NO_EMAIL_ADDRESS_DECISION_WITHOUT_HEARING = "No email address for decisions made without hearing";

    private final String listCaseHearingCentreIsNotPresent = "listCaseHearingCentre is not present";
    private final String noEmailAddressDecisionWithoutHearing = "No email address for decisions made without hearing";
    private final Map<HearingCentre, String> hearingCentreEmailAddresses;
    private final Map<HearingCentre, String> homeOfficeEmailAddresses;
    private final Map<HearingCentre, String> homeOfficeFtpaEmailAddresses;
    private final Map<BailHearingCentre, String> bailHearingCentreEmailAddresses;

    private final Map<HearingCentre, String> adminEmailAddresses;

    private final String listCaseCaseOfficerEmailAddress;


    public EmailAddressFinder(
            Map<HearingCentre, String> hearingCentreEmailAddresses,
            Map<HearingCentre, String> homeOfficeEmailAddresses,
            Map<HearingCentre, String> homeOfficeFtpaEmailAddresses,
            Map<BailHearingCentre, String> bailHearingCentreEmailAddresses,
            Map<HearingCentre, String> adminEmailAddresses,
            @Value("${listCaseCaseOfficerEmailAddress}") String listCaseCaseOfficerEmailAddress) {

        this.hearingCentreEmailAddresses = hearingCentreEmailAddresses;
        this.homeOfficeEmailAddresses = homeOfficeEmailAddresses;
        this.homeOfficeFtpaEmailAddresses = homeOfficeFtpaEmailAddresses;
        this.bailHearingCentreEmailAddresses = bailHearingCentreEmailAddresses;
        this.adminEmailAddresses = adminEmailAddresses;
        this.listCaseCaseOfficerEmailAddress = listCaseCaseOfficerEmailAddress;
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
        return getLegalRepEmailInternalOrLegalRepJourney(asylumCase);
    }

    public String getListCaseHomeOfficeEmailAddress(AsylumCase asylumCase) {
        if (isRemoteHearing(asylumCase) || isDecisionWithoutHearing(asylumCase)) {
            return getHomeOfficeEmailAddress(asylumCase);
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
        if (isRemoteHearing(asylumCase) || isDecisionWithoutHearing(asylumCase)) {
            return getHomeOfficeFtpaEmailAddress(asylumCase);
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

    private BailHearingCentre getBailHearingCentre(BailCase bailCase) {
        return bailCase
            .read(BailCaseFieldDefinition.HEARING_CENTRE, BailHearingCentre.class)
            .orElseThrow(() -> new IllegalStateException("Bail hearingCentre is not present"));
    }

    private String getEmailAddress(Map<HearingCentre, String> emailAddressesMap, HearingCentre hearingCentre) {
        switch (hearingCentre) {
            case GLASGOW_TRIBUNAL_CENTRE:
                return emailAddressesMap.get(HearingCentre.GLASGOW);
            case NOTTINGHAM:
            case COVENTRY:
                return emailAddressesMap.get(HearingCentre.BIRMINGHAM);
            case DECISION_WITHOUT_HEARING:
                return noEmailAddressDecisionWithoutHearing;
            default:
                return emailAddressesMap.get(hearingCentre);
        }
    }


    private String getAdminHearingCentreAddress(Map<HearingCentre, String> emailAddressesMap, HearingCentre hearingCentre) {
        switch (hearingCentre) {
            case GLASGOW_TRIBUNAL_CENTRE:
            case BELFAST:
                return emailAddressesMap.get(HearingCentre.GLASGOW);
            case NOTTINGHAM:
            case COVENTRY:
                return emailAddressesMap.get(HearingCentre.BIRMINGHAM);
            case NEWCASTLE:
                return emailAddressesMap.get(HearingCentre.BRADFORD);
            default:
                return emailAddressesMap.get(hearingCentre);
        }
    }

    public String getAdminEmailAddress(AsylumCase asylumCase) {
        return asylumCase
                .read(HEARING_CENTRE, HearingCentre.class)
                .map(it -> Optional.ofNullable(getAdminHearingCentreAddress(adminEmailAddresses, it))
                        .orElseThrow(() -> new IllegalStateException("Hearing centre email address not found: " + it.toString()))
                )
                .orElseThrow(() -> new IllegalStateException("hearingCentre is not present"));
    }


    public String getHomeOfficeEmailAddress(AsylumCase asylumCase) {
        return asylumCase
                .read(HEARING_CENTRE, HearingCentre.class)
                .map(it -> Optional.ofNullable(getEmailAddress(homeOfficeEmailAddresses, it))
                        .orElseThrow(() -> new IllegalStateException("Hearing centre email address not found: " + it.toString()))
                )
                .orElseThrow(() -> new IllegalStateException("hearingCentre is not present"));
    }

    public String getHomeOfficeFtpaEmailAddress(AsylumCase asylumCase) {
        return asylumCase
                .read(HEARING_CENTRE, HearingCentre.class)
                .map(it -> Optional.ofNullable(getEmailAddress(homeOfficeFtpaEmailAddresses, it))
                        .orElseThrow(() -> new IllegalStateException("Hearing centre email address not found: " + it.toString()))
                )
                .orElseThrow(() -> new IllegalStateException("hearingCentre is not present"));
    }

    public String getBailHearingCentreEmailAddress(BailCase bailCase) {

        final BailHearingCentre hearingCentre =
            getBailHearingCentre(bailCase);

        final String bailHearingCentreEmailAddress =
            bailHearingCentreEmailAddresses
                .get(hearingCentre);

        if (bailHearingCentreEmailAddress == null) {
            throw new IllegalStateException("Hearing centre email address not found: " + hearingCentre.toString());
        }

        return bailHearingCentreEmailAddress;
    }

    private boolean isRemoteHearing(AsylumCase asylumCase) {
        return asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
            .map(hearingCentre -> hearingCentre == HearingCentre.REMOTE_HEARING)
            .orElse(false);
    }

    private boolean isDecisionWithoutHearing(AsylumCase asylumCase) {
        return AsylumCaseUtils.isDecisionWithoutHearingAppeal(asylumCase)
               || asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                   .map(hearingCentre -> hearingCentre == HearingCentre.DECISION_WITHOUT_HEARING)
                   .orElse(false);
    }



    public String getListCaseCaseOfficerHearingCentreEmailAddress(AsylumCase asylumCase) {
        if (isRemoteHearing(asylumCase)) {
            final HearingCentre hearingCentre = getHearingCentre(asylumCase, HEARING_CENTRE);
            if (Arrays.asList(HearingCentre.GLASGOW, HearingCentre.BELFAST).contains(hearingCentre)) {
                return listCaseCaseOfficerEmailAddress;
            } else {
                return getHearingCentreEmailAddress(asylumCase);
            }
        } else {
            return asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class).map(hearingCentre -> {
                if (Arrays.asList(HearingCentre.GLASGOW, HearingCentre.BELFAST).contains(hearingCentre)) {
                    return listCaseCaseOfficerEmailAddress;
                } else {
                    return getEmailAddress(hearingCentreEmailAddresses, hearingCentre);
                }
            }).orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));
        }
    }

}
