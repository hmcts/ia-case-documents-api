package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class HomeOfficeAppealSubmittedPendingPaymentPersonalisation implements EmailNotificationPersonalisation {

    private final String homeOfficeAppealSubmittedPendingPaymentTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final String apcPrivateBetaInboxHomeOfficeEmailAddress;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public HomeOfficeAppealSubmittedPendingPaymentPersonalisation(
            @NotNull(message = "pendingPaymentHomeOfficeTemplateId cannot be null")
            @Value("${govnotify.template.appealSubmitted.homeOffice.pendingPaymentEaHu.email}") String homeOfficeAppealSubmittedPendingPaymentTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            CustomerServicesProvider customerServicesProvider,
            @Value("${apcPrivateHomeOfficeEmailAddress}") String apcPrivateBetaInboxHomeOfficeEmailAddress
    ) {
        this.homeOfficeAppealSubmittedPendingPaymentTemplateId = homeOfficeAppealSubmittedPendingPaymentTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.apcPrivateBetaInboxHomeOfficeEmailAddress = apcPrivateBetaInboxHomeOfficeEmailAddress;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_SUBMITTED_PENDING_PAYMENT_HOME_OFFICE";
    }

    @Override
    public String getTemplateId() {
        return homeOfficeAppealSubmittedPendingPaymentTemplateId;
    }



    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(apcPrivateBetaInboxHomeOfficeEmailAddress);
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
    }
}
