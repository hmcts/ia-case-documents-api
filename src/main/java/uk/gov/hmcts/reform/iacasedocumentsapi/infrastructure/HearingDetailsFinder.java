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
        final HearingCentre listCaseHearingCentre =
            getHearingCentre(asylumCase);

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

}

