package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.CommonUtils.convertAsylumCaseFeeValue;

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
public class AipRemissionRequestAutomaticReminderEmail implements EmailNotificationPersonalisation {

    private final String paymentRejectedReminderTemplateId;
    private final String paymentPartiallyApprovedReminderTemplateId;
    private final RecipientsFinder recipientsFinder;
    private final String iaAipFrontendUrl;

    public AipRemissionRequestAutomaticReminderEmail(
        @Value("${govnotify.template.remissionDecisionReminder.appellant.rejected.email}") String paymentRejectedReminderTemplateId,
        @Value("${govnotify.template.remissionDecisionReminder.appellant.partiallyApproved.email}") String paymentPartiallyApprovedReminderTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl, RecipientsFinder recipientsFinder
    ) {
        this.paymentRejectedReminderTemplateId = paymentRejectedReminderTemplateId;
        this.paymentPartiallyApprovedReminderTemplateId = paymentPartiallyApprovedReminderTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        RemissionDecision remissionDecision = asylumCase.read(AsylumCaseDefinition.REMISSION_DECISION, RemissionDecision.class)
            .orElseThrow(() -> new IllegalStateException("Remission decision not found"));

        if (remissionDecision.equals(RemissionDecision.PARTIALLY_APPROVED)) {
            return paymentPartiallyApprovedReminderTemplateId;
        } else if (remissionDecision.equals(RemissionDecision.REJECTED)) {
            return paymentRejectedReminderTemplateId;
        }
        throw new IllegalStateException("error retrieving remission decision");
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_AIP_REMISSION_REMINDER_DECISION_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("feeAmount", convertAsylumCaseFeeValue(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class).orElse("")))
            .put("deadline", asylumCase.read(REMISSION_REJECTED_DATE_PLUS_14DAYS, String.class).orElse(""))
            .put("onlineCaseReferenceNumber", asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class).orElse(""))
            .put("feeAmountRejected", convertAsylumCaseFeeValue(asylumCase.read(FEE_AMOUNT_GBP, String.class).orElse("")))
            .put("linkToOnlineService", iaAipFrontendUrl)
            .build();
    }
}

