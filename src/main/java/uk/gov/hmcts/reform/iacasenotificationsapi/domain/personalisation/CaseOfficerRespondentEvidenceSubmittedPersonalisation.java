package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;

@Service
public class CaseOfficerRespondentEvidenceSubmittedPersonalisation implements NotificationPersonalisation {

    private final String respondentEvidenceSubmittedTemplateId;
    private final Map<HearingCentre, String> hearingCentreEmailAddresses;

    public CaseOfficerRespondentEvidenceSubmittedPersonalisation(
            @NotNull(message = "respondentEvidenceSubmittedTemplateId cannot be null") @Value("${govnotify.template.respondentEvidenceSubmittedTemplateId}")String respondentEvidenceSubmittedTemplateId,
            Map<HearingCentre, String> hearingCentreEmailAddresses
    ) {
        this.respondentEvidenceSubmittedTemplateId = respondentEvidenceSubmittedTemplateId;
        this.hearingCentreEmailAddresses = hearingCentreEmailAddresses;
    }

    @Override
    public String getTemplateId() {
        return respondentEvidenceSubmittedTemplateId;
    }

    @Override
    public String getEmailAddress(AsylumCase asylumCase) {
        final HearingCentre hearingCentre =
                asylumCase
                        .read(HEARING_CENTRE, HearingCentre.class)
                        .orElseThrow(() -> new IllegalStateException("hearingCentre is not present"));

        final String hearingCentreEmailAddress =
                hearingCentreEmailAddresses
                        .get(hearingCentre);

        if (null == hearingCentreEmailAddress) {
            throw new IllegalStateException("Hearing centre email address not found: " + hearingCentre.toString());
        }

        return hearingCentreEmailAddress;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RESPONDENT_EVIDENCE_SUBMITTED_CASE_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
                ImmutableMap
                        .<String, String>builder()
                        .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                        .build();
    }
}
