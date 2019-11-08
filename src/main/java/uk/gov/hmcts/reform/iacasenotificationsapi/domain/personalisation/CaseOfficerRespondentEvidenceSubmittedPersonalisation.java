package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@Service
public class CaseOfficerRespondentEvidenceSubmittedPersonalisation implements NotificationPersonalisation {

    private final String respondentEvidenceSubmittedTemplateId;
    private final EmailAddressFinder emailAddressFinder;

    public CaseOfficerRespondentEvidenceSubmittedPersonalisation(
        @NotNull(message = "respondentEvidenceSubmittedTemplateId cannot be null") @Value("${govnotify.template.respondentEvidenceSubmittedTemplateId}") String respondentEvidenceSubmittedTemplateId,
        EmailAddressFinder emailAddressFinder
    ) {
        this.respondentEvidenceSubmittedTemplateId = respondentEvidenceSubmittedTemplateId;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId() {
        return respondentEvidenceSubmittedTemplateId;
    }

    @Override
    public String getEmailAddress(AsylumCase asylumCase) {

        return emailAddressFinder.getEmailAddress(asylumCase);
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
