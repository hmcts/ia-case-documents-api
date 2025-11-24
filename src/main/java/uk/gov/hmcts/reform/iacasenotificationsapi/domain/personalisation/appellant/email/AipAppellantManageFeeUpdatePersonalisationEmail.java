package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT_GBP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FEE_UPDATE_REASON;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.MANAGE_FEE_REQUESTED_AMOUNT;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.PREVIOUS_FEE_AMOUNT_GBP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.CommonUtils.convertAsylumCaseFeeValue;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FeeUpdateReason;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@Service
public class AipAppellantManageFeeUpdatePersonalisationEmail implements EmailNotificationPersonalisation {

    private final String aipAppellantManageFeeUpdateTemplateId;
    private final String iaAipFrontendUrl;
    private final int daysAfterNotificationSent;
    private final CustomerServicesProvider customerServicesProvider;
    private final RecipientsFinder recipientsFinder;
    private final SystemDateProvider systemDateProvider;
    private final FeatureToggler featureToggler;

    public AipAppellantManageFeeUpdatePersonalisationEmail(
        @Value("${govnotify.template.manageFeeUpdate.appellant.email}") String aipAppellantManageFeeUpdateTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        @Value("${appellantDaysToWait.afterNotificationSent}") int daysAfterNotificationSent,
        CustomerServicesProvider customerServicesProvider,
        RecipientsFinder recipientsFinder,
        SystemDateProvider systemDateProvider,
        FeatureToggler featureToggler
    ) {
        this.aipAppellantManageFeeUpdateTemplateId = aipAppellantManageFeeUpdateTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.daysAfterNotificationSent = daysAfterNotificationSent;
        this.customerServicesProvider = customerServicesProvider;
        this.recipientsFinder = recipientsFinder;
        this.systemDateProvider = systemDateProvider;
        this.featureToggler = featureToggler;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_MANAGE_FEE_UPDATE_AIP_APPELLANT_EMAIL";
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return aipAppellantManageFeeUpdateTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return featureToggler.getValue("dlrm-telephony-feature-flag", false)
            ? recipientsFinder.findAll(asylumCase, NotificationType.EMAIL)
            : Collections.emptySet();
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
            .put("respondentReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("onlineCaseReferenceNumber", asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class).orElse(""))
            .put("linkToService", iaAipFrontendUrl)
            .put("originalTotalFee", convertAsylumCaseFeeValue(asylumCase.read(PREVIOUS_FEE_AMOUNT_GBP, String.class).orElse("")))
            .put("newTotalFee", convertAsylumCaseFeeValue(asylumCase.read(FEE_AMOUNT_GBP, String.class).orElse("")))
            .put("dueDate", dueDate)
            .put("feeUpdateReason", asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class)
                .map(FeeUpdateReason::getNormalizedValue)
                .orElse(""))
            .put("paymentAmount", convertAsylumCaseFeeValue(asylumCase.read(MANAGE_FEE_REQUESTED_AMOUNT, String.class).orElse("")))
            .build();
    }
}
