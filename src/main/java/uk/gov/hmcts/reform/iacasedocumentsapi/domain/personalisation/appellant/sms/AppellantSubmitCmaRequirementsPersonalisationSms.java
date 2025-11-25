package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

@Service
public class AppellantSubmitCmaRequirementsPersonalisationSms implements SmsNotificationPersonalisation {

    private final String cmaRequirementsSubmittedAppellantSmsTemplateId;
    private final String iaAipFrontendUrl;
    private final int daysToWaitAfterCmaRequirements;
    private final RecipientsFinder recipientsFinder;
    private final SystemDateProvider systemDateProvider;

    public AppellantSubmitCmaRequirementsPersonalisationSms(
        @Value("${govnotify.template.submitCmaRequirements.appellant.sms}") String cmaRequirementsSubmittedAppellantSmsTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        @Value("${appellantDaysToWait.afterCmaRequirements}") int daysToWaitAfterCmaRequirements,
        RecipientsFinder recipientsFinder,
        SystemDateProvider systemDateProvider
    ) {
        this.cmaRequirementsSubmittedAppellantSmsTemplateId = cmaRequirementsSubmittedAppellantSmsTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.daysToWaitAfterCmaRequirements = daysToWaitAfterCmaRequirements;
        this.recipientsFinder = recipientsFinder;
        this.systemDateProvider = systemDateProvider;
    }


    @Override
    public String getTemplateId() {
        return cmaRequirementsSubmittedAppellantSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_SUBMIT_CMA_REQUIREMENTS_APPELLANT_AIP_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        final String dueDate = systemDateProvider.dueDate(daysToWaitAfterCmaRequirements);

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("due date", dueDate)
                .put("Hyperlink to service", iaAipFrontendUrl)
                .build();
    }
}
