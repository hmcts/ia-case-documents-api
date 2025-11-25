package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealType;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.PA_APPEAL_TYPE_AIP_PAYMENT_OPTION;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Service
public class AppellantSubmittedWithRemissionRequestPersonalisationSms implements SmsNotificationPersonalisation {
    private final String submittedRemissionRequestSmsTemplateId;
    private final String submittedRemissionRequestPaPayLaterSmsTemplateId;
    private final RecipientsFinder recipientsFinder;
    private final int daysToWaitAfterSubmittingAppealRemission;
    private final String iaAipFrontendUrl;
    private final SystemDateProvider systemDateProvider;
    private final int daysAfterAppealSubmitted;


    public AppellantSubmittedWithRemissionRequestPersonalisationSms(
        @Value("${govnotify.template.appealSubmitted.appellant.remission.sms}") String submittedRemissionRequestSmsTemplateId,
        @Value("${govnotify.template.appealSubmitted.appellant.remission.paPayLater.sms}") String submittedRemissionRequestPaPayLaterSmsTemplateId,
        @Value("${appellantDaysToWait.afterHearingRequirementsSubmitted}") int daysAfterAppealSubmitted,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        @Value("${appellantDaysToWait.afterSubmittingAppealRemission}") int daysToWaitAfterSubmittingAppealRemission,
        RecipientsFinder recipientsFinder,
        SystemDateProvider systemDateProvider
    ) {
        this.submittedRemissionRequestSmsTemplateId = submittedRemissionRequestSmsTemplateId;
        this.submittedRemissionRequestPaPayLaterSmsTemplateId = submittedRemissionRequestPaPayLaterSmsTemplateId;
        this.recipientsFinder = recipientsFinder;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.daysToWaitAfterSubmittingAppealRemission = daysToWaitAfterSubmittingAppealRemission;
        this.systemDateProvider = systemDateProvider;
        this.daysAfterAppealSubmitted = daysAfterAppealSubmitted;
    }


    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        Optional<AppealType> maybeAppealType = asylumCase.read(APPEAL_TYPE, AppealType.class);

        if (maybeAppealType.isPresent() && maybeAppealType.get() == AppealType.PA) {
            Optional<String> maybePaymentOption = asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class);

            if (maybePaymentOption.isPresent()) {
                String paymentOption = maybePaymentOption.get();
                if ("payLater".equals(paymentOption) || "payOffline".equals(paymentOption)) {
                    return submittedRemissionRequestPaPayLaterSmsTemplateId;
                }
            }
        }
        return submittedRemissionRequestSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_SUBMITTED_WITH_REMISSION_REQUEST_AIP_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        final String dueDate = systemDateProvider.dueDate(daysAfterAppealSubmitted);
        final String refundRequestDueDate = systemDateProvider.dueDate(daysToWaitAfterSubmittingAppealRemission);
        if (getTemplateId(asylumCase).equals(submittedRemissionRequestPaPayLaterSmsTemplateId)) {
            return ImmutableMap
                    .<String, String>builder()
                    .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                    .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                    .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                    .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                    .put("Hyperlink to service", iaAipFrontendUrl)
                    .put("linkToService", iaAipFrontendUrl)
                    .put("14 days after remission request sent", refundRequestDueDate)
                    .build();
        } else {
            return
                    ImmutableMap
                            .<String, String>builder()
                            .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                            .put("appealSubmittedDaysAfter", dueDate)
                            .put("Hyperlink to service", iaAipFrontendUrl)
                            .build();
        }
    }

}
