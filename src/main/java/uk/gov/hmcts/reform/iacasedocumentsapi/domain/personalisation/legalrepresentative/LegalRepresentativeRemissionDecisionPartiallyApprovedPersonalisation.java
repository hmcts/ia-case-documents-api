package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class LegalRepresentativeRemissionDecisionPartiallyApprovedPersonalisation
    implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String partiallyApprovedTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final FeatureToggler featureToggler;

    public LegalRepresentativeRemissionDecisionPartiallyApprovedPersonalisation(
        @Value("${govnotify.template.remissionDecision.legalRep.partiallyApproved.email}")
            String partiallyApprovedTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        CustomerServicesProvider customerServicesProvider,
        FeatureToggler featureToggler
    ) {
        this.partiallyApprovedTemplateId = partiallyApprovedTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.featureToggler = featureToggler;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return partiallyApprovedTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REMISSION_DECISION_PARTIALLY_APPROVED_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return featureToggler.getValue("dlrm-telephony-feature-flag", false)
                ? LegalRepresentativeEmailNotificationPersonalisation.super.getRecipientsList(asylumCase)
                : Collections.emptySet();
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        String amountLeftToPay = asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)
            .orElseThrow(() -> new IllegalStateException("Amount left to pay is not present"));
        BigDecimal amountLeftToPayInGbp = new BigDecimal(String.valueOf(Double.valueOf(amountLeftToPay) / 100))
            .setScale(2, RoundingMode.DOWN);
        return ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("feeAmount", amountLeftToPayInGbp.toString())
            .put("14 days after remission decision", asylumCase.read(REMISSION_REJECTED_DATE_PLUS_14DAYS, String.class).orElse(""))
            .put("onlineCaseReferenceNumber", asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class).orElse(""))
            .build();
    }
}
