package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.caseofficer;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealType.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;

@Service
public class CaseOfficerManageFeeUpdatePersonalisation implements EmailNotificationPersonalisation {

    private final String ctscManageFeeUpdateBeforeListingTemplateId;
    private final String ctscManageFeeUpdateAfterListingTemplateId;
    private final String nbcManageFeeUpdateBeforeListingTemplateId;
    private final String nbcManageFeeUpdateAfterListingTemplateId;
    private final String nbcEmailAddress;
    private final String ctscEmailAddress;
    private final String iaExUiFrontendUrl;
    private final FeatureToggler featureToggler;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public CaseOfficerManageFeeUpdatePersonalisation(
            @Value("${govnotify.template.manageFeeUpdate.ctsc.beforeListing.email}") String ctscManageFeeUpdateBeforeListingTemplateId,
            @Value("${govnotify.template.manageFeeUpdate.ctsc.afterListing.email}") String ctscManageFeeUpdateAfterListingTemplateId,
            @Value("${govnotify.template.manageFeeUpdate.nbc.beforeListing.email}") String nbcManageFeeUpdateBeforeListingTemplateId,
            @Value("${govnotify.template.manageFeeUpdate.nbc.afterListing.email}") String nbcManageFeeUpdateAfterListingTemplateId,
            @Value("${nbcEmailAddress}") String nbcEmailAddress,
            @Value("${ctscEmailAddress}") String ctscEmailAddress,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            FeatureToggler featureToggler) {
        this.ctscManageFeeUpdateBeforeListingTemplateId = ctscManageFeeUpdateBeforeListingTemplateId;
        this.ctscManageFeeUpdateAfterListingTemplateId = ctscManageFeeUpdateAfterListingTemplateId;
        this.nbcManageFeeUpdateBeforeListingTemplateId = nbcManageFeeUpdateBeforeListingTemplateId;
        this.nbcManageFeeUpdateAfterListingTemplateId = nbcManageFeeUpdateAfterListingTemplateId;
        this.nbcEmailAddress = nbcEmailAddress;
        this.ctscEmailAddress = ctscEmailAddress;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.featureToggler = featureToggler;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        if (featureToggler.getValue("tcw-notifications-feature", true)) {
            if (isPaymentByPBa(asylumCase)) {
                return Collections.singleton(ctscEmailAddress);
            } else if (isPaymentByCard(asylumCase)) {
                return Collections.singleton(nbcEmailAddress);
            }
        } else {
            return Collections.emptySet();
        }
        throw new IllegalStateException("Email Address cannot be found");
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        if (isPaymentByCard(asylumCase)) {
            return isAppealListed(asylumCase)
                ? nbcManageFeeUpdateAfterListingTemplateId : nbcManageFeeUpdateBeforeListingTemplateId;

        } else if (isPaymentByPBa(asylumCase)) {
            return isAppealListed(asylumCase)
                ? ctscManageFeeUpdateAfterListingTemplateId : ctscManageFeeUpdateBeforeListingTemplateId;
        } else {
            throw new IllegalStateException("Template cannot be found");
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_MANAGE_FEE_UPDATE_CASE_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .put("ariaListingReference", asylumCase.read(AsylumCaseDefinition.ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
            .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }

    protected boolean isPaymentByCard(AsylumCase asylumCase) {

        String eaHuAppealTypePaymentOption = asylumCase
            .read(AsylumCaseDefinition.EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class).orElse("");

        String paAppealTypePaymentOption = asylumCase
            .read(AsylumCaseDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class).orElse("");

        return (isEaHuEuAppeal(asylumCase) && eaHuAppealTypePaymentOption.equals("payOffline"))
               || (isPaAppeal(asylumCase) && paAppealTypePaymentOption.equals("payOffline"));
    }

    protected boolean isPaymentByPBa(AsylumCase asylumCase) {
        String eaHuAppealTypePaymentOption = asylumCase
            .read(AsylumCaseDefinition.EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class).orElse("");

        String paAppealTypePaymentOption = asylumCase
            .read(AsylumCaseDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class).orElse("");

        return (isEaHuEuAppeal(asylumCase) && eaHuAppealTypePaymentOption.equals("payNow"))
                 || (isPaAppeal(asylumCase) &&  Arrays.asList("payNow","payLater").contains(paAppealTypePaymentOption));
    }

    protected boolean isPaAppeal(AsylumCase asylumCase) {
        return asylumCase
            .read(APPEAL_TYPE, AppealType.class)
            .map(type -> type == PA).orElse(false);
    }

    protected boolean isEaHuEuAppeal(AsylumCase asylumCase) {
        return asylumCase
            .read(APPEAL_TYPE, AppealType.class)
            .map(type -> type == EA || type == HU || type == EU).orElse(false);
    }
}
