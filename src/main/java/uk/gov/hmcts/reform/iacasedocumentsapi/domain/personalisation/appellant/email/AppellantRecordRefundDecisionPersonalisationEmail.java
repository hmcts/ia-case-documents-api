package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionDecision.APPROVED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionDecision.PARTIALLY_APPROVED;

@Service
public class AppellantRecordRefundDecisionPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String appellantRefundApprovedTemplateId;
    private final String appellantRefundPartiallyApprovedTemplateId;
    private final String appellantRefundRejectedTemplateId;
    private final String iaAipFrontendUrl;
    private final int daysAfterRefundDecision;
    private final CustomerServicesProvider customerServicesProvider;
    private final RecipientsFinder recipientsFinder;
    private final SystemDateProvider systemDateProvider;

    public AppellantRecordRefundDecisionPersonalisationEmail(
        @Value("${govnotify.template.recordRefundDecision.appellant.approved.email}") String appellantRefundApprovedTemplateId,
        @Value("${govnotify.template.recordRefundDecision.appellant.partiallyApproved.email}") String appellantRefundPartiallyApprovedTemplateId,
        @Value("${govnotify.template.recordRefundDecision.appellant.rejected.email}") String appellantRefundRejectedTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        @Value("${appellantDaysToWait.afterRemissionDecision}") int daysAfterRefundDecision,
        CustomerServicesProvider customerServicesProvider,
        RecipientsFinder recipientsFinder,
        SystemDateProvider systemDateProvider
    ) {
        this.appellantRefundApprovedTemplateId = appellantRefundApprovedTemplateId;
        this.appellantRefundPartiallyApprovedTemplateId = appellantRefundPartiallyApprovedTemplateId;
        this.appellantRefundRejectedTemplateId = appellantRefundRejectedTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.daysAfterRefundDecision = daysAfterRefundDecision;
        this.customerServicesProvider = customerServicesProvider;
        this.recipientsFinder = recipientsFinder;
        this.systemDateProvider = systemDateProvider;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REFUND_DECISION_DECIDED_AIP_APPELLANT_EMAIL";
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        RemissionDecision remissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
            .orElseThrow(() -> new IllegalStateException("Remission decision not found"));

        return switch (remissionDecision) {
            case APPROVED -> appellantRefundApprovedTemplateId;
            case PARTIALLY_APPROVED -> appellantRefundPartiallyApprovedTemplateId;
            case REJECTED -> appellantRefundRejectedTemplateId;
        };
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final String dueDate = systemDateProvider.dueDate(daysAfterRefundDecision);

        return ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("linkToService", iaAipFrontendUrl)
            .put("14DaysAfterRefundDecision", dueDate)
            .put("refundAmount", calculateAmountRefunded(asylumCase))
            .build();
    }

    private String calculateAmountRefunded(AsylumCase asylumCase) {
        RemissionDecision remissionDecision = asylumCase.read(
                AsylumCaseDefinition.REMISSION_DECISION,
                RemissionDecision.class
            )
            .orElseThrow(() -> new IllegalStateException("Remission decision not found"));

        if (remissionDecision.equals(PARTIALLY_APPROVED) || remissionDecision.equals(APPROVED)) {
            String amountRemitted = asylumCase.read(AMOUNT_REMITTED, String.class).orElse("");

            BigDecimal amountRemittedInGbp = new BigDecimal(String.valueOf(Double.parseDouble(amountRemitted) / 100))
                .setScale(2, RoundingMode.DOWN);

            return amountRemittedInGbp.toString();
        }
        return "";
    }
}
