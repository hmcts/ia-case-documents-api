package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.CaseOfficerCaseEditedPersonalisationFactory;

@Component
public class CaseOfficerCaseEditedNotifier implements CaseEmailNotifier {

    private final CaseOfficerCaseEditedPersonalisationFactory caseOfficerCaseEditedPersonalisationFactory;
    private final Map<HearingCentre, String> hearingCentreEmailAddresses;

    public CaseOfficerCaseEditedNotifier(
        CaseOfficerCaseEditedPersonalisationFactory caseOfficerCaseEditedPersonalisationFactory,
        Map<HearingCentre, String> hearingCentreEmailAddresses
    ) {
        requireNonNull(caseOfficerCaseEditedPersonalisationFactory, "caseOfficerCaseEditedPersonalisationFactory must not be null");
        requireNonNull(hearingCentreEmailAddresses, "hearingCentreEmailAddresses must not be null");

        this.caseOfficerCaseEditedPersonalisationFactory = caseOfficerCaseEditedPersonalisationFactory;
        this.hearingCentreEmailAddresses = hearingCentreEmailAddresses;
    }

    @Override
    public String getEmailAddress(AsylumCase asylumCase) {

        String hearingCentreEmailAddress =
            asylumCase
                .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .map(it -> Optional.ofNullable(hearingCentreEmailAddresses.get(it))
                    .orElseThrow(() -> new IllegalStateException("Hearing centre email address not found: " + it.toString()))
                )
                .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));

        return hearingCentreEmailAddress;
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {

        return caseOfficerCaseEditedPersonalisationFactory
                .createEditedCase(asylumCase, null);
    }

    public Map<String, String> getPersonalisation(AsylumCase asylumCase, Optional<CaseDetails<AsylumCase>> caseDetailsBefore) {

        return caseOfficerCaseEditedPersonalisationFactory
                .createEditedCase(asylumCase, caseDetailsBefore);
    }
}
