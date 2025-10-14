package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class LegalRepresentativePendingPaymentPaidPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepresentativePendingPaymentPaidBeforeListingTemplateId;
    private final String legalRepresentativePendingPaymentPaidAfterListingTemplateId;
    private final String legalRepresentativePendingPaymentPaidEaHuTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;

    public LegalRepresentativePendingPaymentPaidPersonalisation(
            @Value("${govnotify.template.pendingPaymentBeforeListing.legalRep.paid.email}") String legalRepresentativePendingPaymentPaidBeforeListingTemplateId,
            @Value("${govnotify.template.pendingPaymentAfterListing.legalRep.paid.email}") String legalRepresentativePendingPaymentPaidAfterListingTemplateId,
            @Value("${govnotify.template.pendingPaymentEaHu.legalRep.paid.email}") String legalRepresentativePendingPaymentPaidEaHuTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            CustomerServicesProvider customerServicesProvider
    ) {
        requireNonNull(iaExUiFrontendUrl, "iaExUiFrontendUrl must not be null");
        this.legalRepresentativePendingPaymentPaidBeforeListingTemplateId = legalRepresentativePendingPaymentPaidBeforeListingTemplateId;
        this.legalRepresentativePendingPaymentPaidAfterListingTemplateId = legalRepresentativePendingPaymentPaidAfterListingTemplateId;
        this.legalRepresentativePendingPaymentPaidEaHuTemplateId = legalRepresentativePendingPaymentPaidEaHuTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
    }


    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
                .orElseThrow(() -> new IllegalStateException("AppealType is not present"));

        String template = "";

        switch (appealType) {
            case EA:
            case EU:
            case HU:
                template = legalRepresentativePendingPaymentPaidEaHuTemplateId;
                break;
            case PA:
                template = isAppealListed(asylumCase)
                        ? legalRepresentativePendingPaymentPaidAfterListingTemplateId : legalRepresentativePendingPaymentPaidBeforeListingTemplateId;
                break;
            default:
                template = "";
        }
        return template;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_PENDING_PAYMENT_PAID_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
                ImmutableMap
                        .<String, String>builder()
                        .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                        .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
                        .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
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
}
