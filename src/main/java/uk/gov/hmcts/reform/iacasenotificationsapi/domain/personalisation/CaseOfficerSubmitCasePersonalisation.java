package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@Service
public class CaseOfficerSubmitCasePersonalisation implements NotificationPersonalisation {

    private final String submitCaseCaseOfficerTemplateId;
    private final Map<HearingCentre, String> hearingCentreEmailAddresses;

    public CaseOfficerSubmitCasePersonalisation(
        @Value("${govnotify.template.caseOfficerSubmitCase}") String submitCaseCaseOfficerTemplateId,
        Map<HearingCentre, String> hearingCentreEmailAddresses
    ) {

        this.submitCaseCaseOfficerTemplateId = submitCaseCaseOfficerTemplateId;
        this.hearingCentreEmailAddresses = hearingCentreEmailAddresses;
    }

    @Override
    public String getTemplateId() {
        return submitCaseCaseOfficerTemplateId;
    }

    @Override
    public String getEmailAddress(AsylumCase asylumCase) {

        return new EmailAddressFinder(hearingCentreEmailAddresses).getEmailAddress(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CASE_SUBMITTED_CASE_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .build();
    }
}
