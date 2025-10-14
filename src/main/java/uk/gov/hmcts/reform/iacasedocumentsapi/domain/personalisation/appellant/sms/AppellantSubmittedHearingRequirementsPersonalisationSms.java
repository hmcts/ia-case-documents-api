package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;

@Service
public class AppellantSubmittedHearingRequirementsPersonalisationSms implements SmsNotificationPersonalisation {
    private final String submittedHearingRequirementsSmsTemplateId;
    private final RecipientsFinder recipientsFinder;
    private final SystemDateProvider systemDateProvider;
    private final int daysToWaitAfterHearingRequirementsSubmitted;


    public AppellantSubmittedHearingRequirementsPersonalisationSms(
        @Value("${govnotify.template.submittedHearingRequirements.appellant.sms}") String submittedHearingRequirementsSmsTemplateId,
        @Value("${appellantDaysToWait.afterHearingRequirementsSubmitted}") int daysToWaitAfterHearingRequirementsSubmitted,
        RecipientsFinder recipientsFinder,
        SystemDateProvider systemDateProvider
    ) {
        this.submittedHearingRequirementsSmsTemplateId = submittedHearingRequirementsSmsTemplateId;
        this.recipientsFinder = recipientsFinder;
        this.systemDateProvider = systemDateProvider;
        this.daysToWaitAfterHearingRequirementsSubmitted = daysToWaitAfterHearingRequirementsSubmitted;
    }


    @Override
    public String getTemplateId() {
        return submittedHearingRequirementsSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_SUBMITTED_HEARING_REQUIREMENTS_AIP_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        final String dueDate = systemDateProvider.dueDate(daysToWaitAfterHearingRequirementsSubmitted);

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("dueDate", dueDate)
                .build();
    }

}
