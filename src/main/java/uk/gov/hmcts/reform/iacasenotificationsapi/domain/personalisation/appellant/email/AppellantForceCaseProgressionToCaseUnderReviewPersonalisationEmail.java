package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;

@Service
public class AppellantForceCaseProgressionToCaseUnderReviewPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String templateId;
    private final String iaExUiFrontendUrl;

    public AppellantForceCaseProgressionToCaseUnderReviewPersonalisationEmail(
        @NotNull(message = "forceCaseProgressionToCaseUnderReviewAiPTemplateId cannot be null")
        @Value("${govnotify.template.forceCaseProgression.caseBuilding.to.caseUnderReviewAiP.appellant.email}")
            String templateId,
        @Value("${iaExUiFrontendUrl}")
            String iaExUiFrontendUrl) {
        this.templateId = templateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
    }

    public String getTemplateId() {
        return templateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(asylumCase
                .read(APPELLANT_EMAIL_ADDRESS, String.class)
                .orElseThrow(() -> new IllegalStateException("appellantEmailAddress is not present")));
    }

    public String getReferenceId(Long caseId) {
        return caseId + "_FORCE_CASE_TO_CASE_UNDER_REVIEW_AIP_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return ImmutableMap.<String, String>builder()
            .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("appellantEmailAddress", asylumCase.read(APPELLANT_EMAIL_ADDRESS, String.class)
                        .orElseThrow(() -> new IllegalStateException("appellantEmailAddress is not present")))
            .build();
    }
}


