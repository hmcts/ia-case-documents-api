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
public class AiPAppellantRefundRequestedNotificationEmail implements EmailNotificationPersonalisation {
    private final String refundRequestedAipEmailTemplateId;
    private final RecipientsFinder recipientsFinder;
    private final String iaAipFrontendUrl;
    private final SystemDateProvider systemDateProvider;
    private final int daysToWaitAfterSubmittingAppealRemission;

    public AiPAppellantRefundRequestedNotificationEmail(
        @Value("${govnotify.template.requestFeeRemission.appellant.email}") String refundRequestedAipEmailTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        @Value("${appellantDaysToWait.afterSubmittingAppealRemission}") int daysToWaitAfterSubmittingAppealRemission,
        RecipientsFinder recipientsFinder,
        SystemDateProvider systemDateProvider
    ) {
        this.refundRequestedAipEmailTemplateId = refundRequestedAipEmailTemplateId;
        this.recipientsFinder = recipientsFinder;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.daysToWaitAfterSubmittingAppealRemission = daysToWaitAfterSubmittingAppealRemission;
        this.systemDateProvider = systemDateProvider;

    }


    @Override
    public String getTemplateId() {
        return refundRequestedAipEmailTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REFUND_REQUESTED_AIP_NOTIFICATION_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final String refundRequestDueDate = systemDateProvider.dueDate(daysToWaitAfterSubmittingAppealRemission);

        return
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("Hyperlink to service", iaAipFrontendUrl)
                .put("14 days after refund request sent", refundRequestDueDate)
                .build();
    }
}
