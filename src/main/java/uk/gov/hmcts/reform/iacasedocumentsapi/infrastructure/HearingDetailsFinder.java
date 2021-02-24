package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

@Service
public class HearingDetailsFinder {

    private static final String HEARING_CENTRE_ADDRESS = "hearingCentreAddress";

    private final StringProvider stringProvider;

    public HearingDetailsFinder(StringProvider stringProvider) {
        this.stringProvider = stringProvider;
    }

    public String getHearingCentreAddress(AsylumCase asylumCase) {
        final HearingCentre listCaseHearingCentre =
            getHearingCentre(asylumCase);

        final String hearingCentreAddress =
            stringProvider
                .get(HEARING_CENTRE_ADDRESS, listCaseHearingCentre.toString())
                .orElseThrow(() -> new IllegalStateException("hearingCentreAddress is not present"));

        return hearingCentreAddress;
    }

    public String getHearingCentreName(AsylumCase asylumCase) {
        final HearingCentre listCaseHearingCentre = getHearingCentre(asylumCase);

        return stringProvider.get("hearingCentreName", listCaseHearingCentre.toString())
            .orElseThrow(() -> new IllegalStateException("listCaseHearingCentreName is not present"));
    }

    public String getHearingDateTime(AsylumCase asylumCase) {
        return asylumCase
            .read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class)
            .orElseThrow(() -> new IllegalStateException("listCaseHearingDate is not present"));
    }

    private HearingCentre getHearingCentre(AsylumCase asylumCase) {
        return asylumCase
            .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class)
            .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));
    }

    public String getHearingCentreUrl(HearingCentre hearingCentre) {
        String hearingCentreUrl;
        switch (hearingCentre) {
            case BELFAST:
                hearingCentreUrl = "https://www.nidirect.gov.uk/contacts/contacts-az/belfast-laganside-courts";
                break;
            case BIRMINGHAM:
                hearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/birmingham-immigration-and-asylum-chamber-first-tier-tribunal";
                break;
            case BRADFORD:
                hearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/bradford-tribunal-hearing-centre";
                break;
            case GLASGOW:
                hearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/glasgow-employment-and-immigration-tribunals-eagle-building";
                break;
            case HATTON_CROSS:
                hearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/hatton-cross-tribunal-hearing-centre";
                break;
            case TAYLOR_HOUSE:
                hearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/taylor-house-tribunal-hearing-centre";
                break;
            case MANCHESTER:
                hearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/manchester-tribunal-hearing-centre";
                break;
            case NEWPORT:
                hearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/newport-south-wales-immigration-and-asylum-tribunal";
                break;
            case NOTTINGHAM:
                hearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/nottingham-magistrates-court";
                break;
            case NORTH_SHIELDS:
                hearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/newcastle-civil-family-courts-and-tribunals-centre";
                break;
            default:
                hearingCentreUrl = "Hearing centre url not available";
                break;
        }

        return hearingCentreUrl;
    }

}

