package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NEW_FEE_AMOUNT;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.PREVIOUS_DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.CommonUtils.convertAsylumCaseFeeValue;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.normalizeDecisionHearingOptionText;

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@Component
public class LegalRepRefundConfirmationPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String refundConfirmationTemplateId;
    private final String iaExUiFrontendUrl;
    private final SystemDateProvider systemDateProvider;
    private final int daysAfterNotificationSent;
    private final CustomerServicesProvider customerServicesProvider;

    public LegalRepRefundConfirmationPersonalisation(
        @Value("${govnotify.template.refundConfirmation.legalRep.email}") String refundConfirmationTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl, SystemDateProvider systemDateProvider,
        @Value("${appellantDaysToWait.afterNotificationSent}") int daysAfterNotificationSent, CustomerServicesProvider customerServicesProvider
    ) {
        this.refundConfirmationTemplateId = refundConfirmationTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.systemDateProvider = systemDateProvider;
        this.daysAfterNotificationSent = daysAfterNotificationSent;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return refundConfirmationTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REFUND_CONFIRMATION_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final String dueDate = systemDateProvider.dueDate(daysAfterNotificationSent);

        return ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("linkToService", iaExUiFrontendUrl)
            .put("previousDecisionHearingFeeOption", normalizeDecisionHearingOptionText(asylumCase.read(PREVIOUS_DECISION_HEARING_FEE_OPTION, String.class).orElse("")))
            .put("updatedDecisionHearingFeeOption", normalizeDecisionHearingOptionText(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class).orElse("")))
            .put("newFee", convertAsylumCaseFeeValue(asylumCase.read(NEW_FEE_AMOUNT, String.class).orElse("")))
            .put("dueDate", dueDate)
            .build();
    }
}

