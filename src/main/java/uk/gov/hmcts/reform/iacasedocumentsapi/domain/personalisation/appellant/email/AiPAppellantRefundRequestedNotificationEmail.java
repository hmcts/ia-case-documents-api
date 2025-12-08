package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealType;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.PA_APPEAL_TYPE_AIP_PAYMENT_OPTION;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

@Service
public class AiPAppellantRefundRequestedNotificationEmail implements EmailNotificationPersonalisation {
    private final String refundRequestedAipEmailTemplateId;
    private final String refundRequestedAipPaPayLaterEmailTemplateId;
    private final RecipientsFinder recipientsFinder;
    private final String iaAipFrontendUrl;
    private final SystemDateProvider systemDateProvider;
    private final int daysToWaitAfterSubmittingAppealRemission;

    public AiPAppellantRefundRequestedNotificationEmail(
        @Value("${govnotify.template.requestFeeRemission.appellant.email}") String refundRequestedAipEmailTemplateId,
        @Value("${govnotify.template.requestFeeRemission.appellant.paPayLater.email}") String refundRequestedAipPaPayLaterEmailTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        @Value("${appellantDaysToWait.afterSubmittingAppealRemission}") int daysToWaitAfterSubmittingAppealRemission,
        RecipientsFinder recipientsFinder,
        SystemDateProvider systemDateProvider
    ) {
        this.refundRequestedAipEmailTemplateId = refundRequestedAipEmailTemplateId;
        this.refundRequestedAipPaPayLaterEmailTemplateId = refundRequestedAipPaPayLaterEmailTemplateId;
        this.recipientsFinder = recipientsFinder;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.daysToWaitAfterSubmittingAppealRemission = daysToWaitAfterSubmittingAppealRemission;
        this.systemDateProvider = systemDateProvider;

    }


    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        Optional<AppealType> maybeAppealType = asylumCase.read(APPEAL_TYPE, AppealType.class);

        if (maybeAppealType.isPresent() && maybeAppealType.get() == AppealType.PA) {
            Optional<String> maybePaymentOption = asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class);

            if (maybePaymentOption.isPresent()) {
                String paymentOption = maybePaymentOption.get();
                if ("payLater".equals(paymentOption) || "payOffline".equals(paymentOption)) {
                    return refundRequestedAipPaPayLaterEmailTemplateId;
                }
            }
        }

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
        final String correctDateKey = getTemplateId(asylumCase).equals(refundRequestedAipPaPayLaterEmailTemplateId)
                ? "14 days after remission request sent"
                : "14 days after refund request sent";

        return
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("Hyperlink to service", iaAipFrontendUrl)
                .put(correctDateKey, refundRequestDueDate)
                .build();
    }
}
