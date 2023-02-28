package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;



@Service
public class HomeOfficeAppealSubmittedPayOfflinePersonalisation implements EmailNotificationPersonalisation {

    private final String appealSubmittedHomeOfficeTemplateId;
    private final String apcPrivateBetaInboxHomeOfficeEmailAddress;
    private final CustomerServicesProvider customerServicesProvider;
    private final String iaExUiFrontendUrl;

    public HomeOfficeAppealSubmittedPayOfflinePersonalisation(
            @Value("${apcPrivateHomeOfficeEmailAddress}") String apcPrivateBetaInboxHomeOfficeEmailAddress,
            @NotNull(message = "appealSubmittedHomeOfficeTemplateId cannot be null") @Value("${govnotify.template.appealSubmitted.homeOffice.email}") String appealSubmittedHomeOfficeTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            CustomerServicesProvider customerServicesProvider
    ) {
        this.apcPrivateBetaInboxHomeOfficeEmailAddress = apcPrivateBetaInboxHomeOfficeEmailAddress;
        this.appealSubmittedHomeOfficeTemplateId = appealSubmittedHomeOfficeTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return appealSubmittedHomeOfficeTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(apcPrivateBetaInboxHomeOfficeEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_SUBMITTED_PAY_OFFLINE_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
    }

}
