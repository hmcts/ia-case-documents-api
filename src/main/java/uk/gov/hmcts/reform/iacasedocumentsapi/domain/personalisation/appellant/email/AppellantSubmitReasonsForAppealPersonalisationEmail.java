package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;

@Service
public class AppellantSubmitReasonsForAppealPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String reasonsForAppealSubmittedAppellantEmailTemplateId;
    private final String iaAipFrontendUrl;
    private final int daysToWaitAfterReasonsForAppeal;
    private final RecipientsFinder recipientsFinder;
    private final SystemDateProvider systemDateProvider;


    public AppellantSubmitReasonsForAppealPersonalisationEmail(
        @Value("${govnotify.template.submitReasonsForAppeal.appellant.email}") String reasonsForAppealSubmittedAppellantEmailTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        @Value("${appellantDaysToWait.afterReasonsForAppeal}") int daysToWaitAfterReasonsForAppeal,
        RecipientsFinder recipientsFinder,
        SystemDateProvider systemDateProvider
    ) {
        this.reasonsForAppealSubmittedAppellantEmailTemplateId = reasonsForAppealSubmittedAppellantEmailTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.daysToWaitAfterReasonsForAppeal = daysToWaitAfterReasonsForAppeal;
        this.recipientsFinder = recipientsFinder;
        this.systemDateProvider = systemDateProvider;
    }

    @Override
    public String getTemplateId() {
        return reasonsForAppealSubmittedAppellantEmailTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_SUBMIT_REASONS_FOR_APPEAL_APPELLANT_AIP_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        final String dueDate = systemDateProvider.dueDate(daysToWaitAfterReasonsForAppeal);

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("HO Ref Number", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("Given names", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("Family name", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("due date", dueDate)
                .put("Hyperlink to service", iaAipFrontendUrl)
                .build();
    }
}
