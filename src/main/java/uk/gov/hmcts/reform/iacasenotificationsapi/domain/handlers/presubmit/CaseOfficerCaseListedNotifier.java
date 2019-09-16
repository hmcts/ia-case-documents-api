package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.CaseOfficerPersonalisationFactory;

@Component
public class CaseOfficerCaseListedNotifier implements CaseEmailNotifier {

    private final CaseOfficerPersonalisationFactory caseOfficerPersonalisationFactory;
    private final Map<HearingCentre, String> hearingCentreEmailAddresses;

    public CaseOfficerCaseListedNotifier(
        CaseOfficerPersonalisationFactory caseOfficerPersonalisationFactory,
        Map<HearingCentre, String> hearingCentreEmailAddresses
    ) {
        requireNonNull(caseOfficerPersonalisationFactory, "caseOfficerPersonalisationFactory must not be null");
        requireNonNull(hearingCentreEmailAddresses, "hearingCentreEmailAddresses must not be null");

        this.caseOfficerPersonalisationFactory = caseOfficerPersonalisationFactory;
        this.hearingCentreEmailAddresses = hearingCentreEmailAddresses;
    }

    @Override
    public String getEmailAddress(AsylumCase asylumCase) {

        final HearingCentre listCaseHearingCentre =
            asylumCase
                .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));

        final String hearingCentreEmailAddress =
            hearingCentreEmailAddresses
                .get(listCaseHearingCentre);

        if (hearingCentreEmailAddress == null) {
            throw new IllegalStateException("Hearing centre email address not found: " + listCaseHearingCentre.toString());
        }

        return hearingCentreEmailAddress;
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {

        return caseOfficerPersonalisationFactory.createListedCase(asylumCase);
    }
}
