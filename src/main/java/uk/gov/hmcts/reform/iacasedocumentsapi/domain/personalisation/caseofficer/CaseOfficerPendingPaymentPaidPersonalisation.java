package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.caseofficer;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;


@Service
public class CaseOfficerPendingPaymentPaidPersonalisation implements EmailNotificationPersonalisation {

    private final String pendingPaymentPaidCaseOfficerTemplateId;
    private final String iaExUiFrontendUrl;
    private final EmailAddressFinder emailAddressFinder;
    private final FeatureToggler featureToggler;

    public CaseOfficerPendingPaymentPaidPersonalisation(
            @Value("${govnotify.template.pendingPaymentEaHu.caseOfficer.email}") String pendingPaymentPaidCaseOfficerTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            EmailAddressFinder emailAddressFinder,
            FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
        requireNonNull(iaExUiFrontendUrl, "iaExUiFrontendUrl must not be null");

        this.pendingPaymentPaidCaseOfficerTemplateId = pendingPaymentPaidCaseOfficerTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId() {
        return pendingPaymentPaidCaseOfficerTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return featureToggler.getValue("tcw-notifications-feature", false)
                ? Collections.singleton(emailAddressFinder.getHearingCentreEmailAddress(asylumCase))
                : Collections.emptySet();
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_PENDING_PAYMENT_PAID_CASE_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
    }
}
