package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionDecision.PARTIALLY_APPROVED;

import com.google.common.collect.ImmutableMap;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@Service
public class AipAppellantRecordRemissionDecisionPersonalisationSms implements SmsNotificationPersonalisation {

    private final String aipAppellantRemissionApprovedTemplateId;
    private final String aipAppellantRemissionPartiallyApprovedTemplateId;
    private final String aipAppellantRemissionRejectedTemplateId;
    private final String iaAipFrontendUrl;
    private final int daysAfterRemissionDecision;
    private final RecipientsFinder recipientsFinder;
    private final SystemDateProvider systemDateProvider;

    public AipAppellantRecordRemissionDecisionPersonalisationSms(
        @Value("${govnotify.template.remissionDecision.appellant.approved.sms}") String aipAppellantRemissionApprovedTemplateId,
        @Value("${govnotify.template.remissionDecision.appellant.partiallyApproved.sms}") String aipAppellantRemissionPartiallyApprovedTemplateId,
        @Value("${govnotify.template.remissionDecision.appellant.rejected.sms}") String aipAppellantRemissionRejectedTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        @Value("${appellantDaysToWait.afterRemissionDecision}") int daysAfterRemissionDecision,
        RecipientsFinder recipientsFinder,
        SystemDateProvider systemDateProvider
    ) {
        this.aipAppellantRemissionApprovedTemplateId = aipAppellantRemissionApprovedTemplateId;
        this.aipAppellantRemissionPartiallyApprovedTemplateId = aipAppellantRemissionPartiallyApprovedTemplateId;
        this.aipAppellantRemissionRejectedTemplateId = aipAppellantRemissionRejectedTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.daysAfterRemissionDecision = daysAfterRemissionDecision;
        this.recipientsFinder = recipientsFinder;
        this.systemDateProvider = systemDateProvider;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REMISSION_DECISION_DECIDED_AIP_APPELLANT_SMS";
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        RemissionDecision remissionDecision = asylumCase.read(AsylumCaseDefinition.REMISSION_DECISION, RemissionDecision.class)
                .orElseThrow(() -> new IllegalStateException("Remission decision not found"));

        return switch (remissionDecision) {
            case APPROVED -> aipAppellantRemissionApprovedTemplateId;
            case PARTIALLY_APPROVED -> aipAppellantRemissionPartiallyApprovedTemplateId;
            case REJECTED -> aipAppellantRemissionRejectedTemplateId;
        };
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final String dueDate = systemDateProvider.dueDate(daysAfterRemissionDecision);

        return ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("onlineCaseReferenceNumber", asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class).orElse(""))
                .put("linkToService", iaAipFrontendUrl)
                .put("payByDeadline", dueDate)
                .put("remainingFee", calculateRemainingFee(asylumCase))
                .build();
    }

    private String calculateRemainingFee(AsylumCase asylumCase) {
        RemissionDecision remissionDecision = asylumCase.read(AsylumCaseDefinition.REMISSION_DECISION, RemissionDecision.class)
                .orElseThrow(() -> new IllegalStateException("Remission decision not found"));

        if (remissionDecision.equals(PARTIALLY_APPROVED)) {
            String amountLeftToPay = asylumCase.read(AsylumCaseDefinition.AMOUNT_LEFT_TO_PAY, String.class).orElse("");

            BigDecimal amountLeftToPayInGbp = new BigDecimal(String.valueOf(Double.parseDouble(amountLeftToPay) / 100))
                    .setScale(2, RoundingMode.DOWN);

            return amountLeftToPayInGbp.toString();
        }
        return "";
    }
}
