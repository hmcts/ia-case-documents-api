package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealDecision;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;

@Service
public class LegalRepresentativeAppealOutcomePersonalisation implements EmailNotificationPersonalisation {

    private final String appealOutcomeAllowedLegalRepresentativeTemplateId;
    private final String appealOutcomeDismissedLegalRepresentativeTemplateId;

    public LegalRepresentativeAppealOutcomePersonalisation(
        @NotNull(message = "appealOutcomeAllowedLegalRepresentativeTemplateId cannot be null") @Value("${govnotify.template.appealOutcomeAllowedLegalRepresentativeTemplateId}") String appealOutcomeAllowedLegalRepresentativeTemplateId,
        @NotNull(message = "appealOutcomeDismissedLegalRepresentativeTemplateId cannot be null") @Value("${govnotify.template.appealOutcomeDismissedLegalRepresentativeTemplateId}") String appealOutcomeDismissedLegalRepresentativeTemplateId) {

        this.appealOutcomeAllowedLegalRepresentativeTemplateId = appealOutcomeAllowedLegalRepresentativeTemplateId;
        this.appealOutcomeDismissedLegalRepresentativeTemplateId = appealOutcomeDismissedLegalRepresentativeTemplateId;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        final AppealDecision appealOutcomeDecision = asylumCase
            .read(AsylumCaseDefinition.IS_DECISION_ALLOWED, AppealDecision.class)
            .orElseThrow(() -> new IllegalStateException("appealOutcomeDecision is not present"));

        return appealOutcomeDecision.getValue().equals(AppealDecision.ALLOWED.getValue())
            ? appealOutcomeAllowedLegalRepresentativeTemplateId : appealOutcomeDismissedLegalRepresentativeTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(asylumCase
            .read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)
            .orElseThrow(() -> new IllegalStateException("legalRepresentativeEmailAddress is not present")));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_OUTCOME_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReferenceNumber", asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .build();
    }
}
