package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;

@Service
public class AppellantForceCaseProgressionToCaseUnderReviewPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String templateId;
    private final String iaExUiFrontendUrl;
    private final RecipientsFinder recipientsFinder;

    public AppellantForceCaseProgressionToCaseUnderReviewPersonalisationEmail(
        @NotNull(message = "forceCaseProgressionToCaseUnderReviewAiPTemplateId cannot be null")
        @Value("${govnotify.template.forceCaseProgression.caseBuilding.to.caseUnderReviewAiP.appellant.email}")
            String templateId,
        @Value("${iaExUiFrontendUrl}")
            String iaExUiFrontendUrl,
            RecipientsFinder recipientsFinder) {
        this.templateId = templateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.recipientsFinder = recipientsFinder;
    }

    public String getTemplateId() {
        return templateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    public String getReferenceId(Long caseId) {
        return caseId + "_FORCE_CASE_TO_CASE_UNDER_REVIEW_AIP_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return ImmutableMap.<String, String>builder()
            .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .build();
    }
}


