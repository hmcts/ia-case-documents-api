package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.CommonUtils.convertAsylumCaseFeeValue;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.normalizeDecisionHearingOptionText;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@Service
public class AipAppellantRefundConfirmationPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String aipAppellantRefundConfirmationInCountryTemplateId;
    private final String aipAppellantRefundConfirmationOutOfCountryTemplateId;
    private final String iaAipFrontendUrl;
    private final int daysAfterNotificationSent;
    private final CustomerServicesProvider customerServicesProvider;
    private final RecipientsFinder recipientsFinder;
    private final SystemDateProvider systemDateProvider;

    public AipAppellantRefundConfirmationPersonalisationEmail(
        @Value("${govnotify.template.refundConfirmation.appellant.inCountry.email}") String aipAppellantRefundConfirmationInCountryTemplateId,
        @Value("${govnotify.template.refundConfirmation.appellant.outOfCountry.email}") String aipAppellantRefundConfirmationOutOfCountryTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        @Value("${appellantDaysToWait.afterNotificationSent}") int daysAfterNotificationSent,
        CustomerServicesProvider customerServicesProvider,
        RecipientsFinder recipientsFinder,
        SystemDateProvider systemDateProvider
    ) {
        this.aipAppellantRefundConfirmationInCountryTemplateId = aipAppellantRefundConfirmationInCountryTemplateId;
        this.aipAppellantRefundConfirmationOutOfCountryTemplateId = aipAppellantRefundConfirmationOutOfCountryTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.daysAfterNotificationSent = daysAfterNotificationSent;
        this.customerServicesProvider = customerServicesProvider;
        this.recipientsFinder = recipientsFinder;
        this.systemDateProvider = systemDateProvider;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REFUND_CONFIRMATION_AIP_APPELLANT_EMAIL";
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        if (asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).isPresent()) {
            return  aipAppellantRefundConfirmationInCountryTemplateId;
        } else {
            return  aipAppellantRefundConfirmationOutOfCountryTemplateId;
        }
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final String dueDate = systemDateProvider.dueDate(daysAfterNotificationSent);

        return ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("previousDecisionHearingFeeOption", normalizeDecisionHearingOptionText(asylumCase.read(PREVIOUS_DECISION_HEARING_FEE_OPTION, String.class).orElse("")))
            .put("updatedDecisionHearingFeeOption", normalizeDecisionHearingOptionText(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class).orElse("")))
            .put("linkToService", iaAipFrontendUrl)
            .put("newFee", convertAsylumCaseFeeValue(asylumCase.read(NEW_FEE_AMOUNT, String.class).orElse("")))
            .put("dueDate", dueDate)
            .build();
    }
}
