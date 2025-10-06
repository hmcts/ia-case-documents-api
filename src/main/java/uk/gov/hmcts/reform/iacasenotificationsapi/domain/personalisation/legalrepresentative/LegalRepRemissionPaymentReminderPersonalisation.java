package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.CommonUtils.convertAsylumCaseFeeValue;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionDecision;

@Component
public class LegalRepRemissionPaymentReminderPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String paymentRejectedReminderTemplateId;
    private final String paymentPartiallyApprovedReminderTemplateId;
    private final String iaExUiFrontendUrl;

    public LegalRepRemissionPaymentReminderPersonalisation(
        @Value("${govnotify.template.remissionDecisionReminder.legalRep.rejected.email}") String paymentRejectedReminderTemplateId,
        @Value("${govnotify.template.remissionDecisionReminder.legalRep.partiallyApproved.email}") String paymentPartiallyApprovedReminderTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl
    ) {
        this.paymentRejectedReminderTemplateId = paymentRejectedReminderTemplateId;
        this.paymentPartiallyApprovedReminderTemplateId = paymentPartiallyApprovedReminderTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
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
    public String getReferenceId(Long caseId) {
        return caseId + "_REMISSION_REMINDER_DECISION_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("feeAmount", convertAsylumCaseFeeValue(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class).orElse("")))
            .put("feeAmountRejected", convertAsylumCaseFeeValue(asylumCase.read(FEE_AMOUNT_GBP, String.class).orElse("")))
            .put("onlineCaseReferenceNumber", asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class).orElse(""))
            .put("deadline", asylumCase.read(REMISSION_REJECTED_DATE_PLUS_14DAYS, String.class).orElse(""))
            .build();
    }
}

