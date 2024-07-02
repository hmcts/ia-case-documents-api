package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_DECISION_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_DATE;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

@Service
public class HearingDetailsFinder {

    private static final String HEARING_CENTRE_ADDRESS = "hearingCentreAddress";

    public static final String DECISION_WITHOUT_HEARING = "Decision Without Hearing";

    private final StringProvider stringProvider;

    public HearingDetailsFinder(StringProvider stringProvider) {
        this.stringProvider = stringProvider;
    }

    public String getHearingCentreAddress(AsylumCase asylumCase) {

        return stringProvider
            .get(HEARING_CENTRE_ADDRESS, getHearingCentre(asylumCase).toString())
            .orElseThrow(() -> new IllegalStateException("hearingCentreAddress is not present"));
    }

    public String getHearingCentreName(AsylumCase asylumCase) {
        if (isDecisionWithoutHearingAppeal(asylumCase)) {
            return DECISION_WITHOUT_HEARING;
        }

        return stringProvider.get("hearingCentreName", getHearingCentre(asylumCase).toString())
            .orElseThrow(() -> new IllegalStateException("listCaseHearingCentreName is not present"));
    }

    public String getHearingDateTime(AsylumCase asylumCase) {

        return asylumCase
            .read(LIST_CASE_HEARING_DATE, String.class)
            .orElseThrow(() -> new IllegalStateException("listCaseHearingDate is not present"));
    }

    private HearingCentre getHearingCentre(AsylumCase asylumCase) {

        return asylumCase
            .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
            .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));
    }

    public String getHearingCentreUrl(HearingCentre hearingCentre) {

        return switch (hearingCentre) {
            case BELFAST -> "https://www.nidirect.gov.uk/contacts/contacts-az/belfast-laganside-courts";
            case BIRMINGHAM -> "https://courttribunalfinder.service.gov.uk/courts/birmingham-immigration-and-asylum-chamber-first-tier-tribunal";
            case BRADFORD -> "https://courttribunalfinder.service.gov.uk/courts/bradford-tribunal-hearing-centre";
            case GLASGOW -> "https://courttribunalfinder.service.gov.uk/courts/glasgow-employment-and-immigration-tribunals-eagle-building";
            case HATTON_CROSS -> "https://courttribunalfinder.service.gov.uk/courts/hatton-cross-tribunal-hearing-centre";
            case TAYLOR_HOUSE -> "https://courttribunalfinder.service.gov.uk/courts/taylor-house-tribunal-hearing-centre";
            case MANCHESTER -> "https://courttribunalfinder.service.gov.uk/courts/manchester-tribunal-hearing-centre";
            case NEWPORT -> "https://courttribunalfinder.service.gov.uk/courts/newport-south-wales-immigration-and-asylum-tribunal";
            case NOTTINGHAM -> "https://courttribunalfinder.service.gov.uk/courts/nottingham-magistrates-court";
            case NORTH_SHIELDS -> "https://courttribunalfinder.service.gov.uk/courts/newcastle-civil-family-courts-and-tribunals-centre";
            default -> "Hearing centre url not available";
        };
    }

    private boolean isDecisionWithoutHearingAppeal(AsylumCase asylumCase) {
        return asylumCase.read(IS_DECISION_WITHOUT_HEARING, YesOrNo.class)
            .map(yesOrNo -> YesOrNo.YES == yesOrNo).orElse(false);
    }

}

