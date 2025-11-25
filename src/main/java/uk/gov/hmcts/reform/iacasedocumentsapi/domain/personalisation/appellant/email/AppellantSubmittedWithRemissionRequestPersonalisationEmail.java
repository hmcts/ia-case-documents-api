package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Service
public class AppellantSubmittedWithRemissionRequestPersonalisationEmail implements EmailNotificationPersonalisation {
    private final String submittedRemissionRequestEmailTemplateId;
    private final RecipientsFinder recipientsFinder;
    private final SystemDateProvider systemDateProvider;
    private final int daysToWaitAfterHearingRequirementsSubmitted;
    private final String iaAipFrontendUrl;


    public AppellantSubmittedWithRemissionRequestPersonalisationEmail(
        @Value("${govnotify.template.appealSubmitted.appellant.remission.email}") String submittedRemissionRequestEmailTemplateId,
        @Value("${appellantDaysToWait.afterHearingRequirementsSubmitted}") int daysToWaitAfterHearingRequirementsSubmitted,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        RecipientsFinder recipientsFinder,
        SystemDateProvider systemDateProvider
    ) {
        this.submittedRemissionRequestEmailTemplateId = submittedRemissionRequestEmailTemplateId;
        this.recipientsFinder = recipientsFinder;
        this.systemDateProvider = systemDateProvider;
        this.daysToWaitAfterHearingRequirementsSubmitted = daysToWaitAfterHearingRequirementsSubmitted;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
    }


    @Override
    public String getTemplateId() {
        return submittedRemissionRequestEmailTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_SUBMITTED_WITH_REMISSION_REQUEST_AIP_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        final String dueDate = systemDateProvider.dueDate(daysToWaitAfterHearingRequirementsSubmitted);

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("appealSubmittedDaysAfter", dueDate)
                .put("Hyperlink to service", iaAipFrontendUrl)
                .build();
    }

}
