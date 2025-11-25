package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.EmailAddressFinder;

@Service
public class HomeOfficeMarkAppealAsAdaPersonalisation implements EmailNotificationPersonalisation {

    private final String markAppealAsAdaHomeOfficeTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final String apcPrivateBetaInboxHomeOfficeEmailAddress;
    private final EmailAddressFinder emailAddressFinder;


    public HomeOfficeMarkAppealAsAdaPersonalisation(
            @NotNull(message = "markAppealAsAdaBeforeListingHomeOfficeTemplateId cannot be null")
            @Value("${govnotify.template.markAppealAsAda.homeOffice.email}") String markAppealAsAdaHomeOfficeTemplateId,
            @Value("${apcPrivateHomeOfficeEmailAddress}") String apcPrivateBetaInboxHomeOfficeEmailAddress,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            CustomerServicesProvider customerServicesProvider,
            EmailAddressFinder emailAddressFinder
    ) {
        this.markAppealAsAdaHomeOfficeTemplateId = markAppealAsAdaHomeOfficeTemplateId;
        this.apcPrivateBetaInboxHomeOfficeEmailAddress = apcPrivateBetaInboxHomeOfficeEmailAddress;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return markAppealAsAdaHomeOfficeTemplateId;
    }


    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(apcPrivateBetaInboxHomeOfficeEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_MARK_APPEAL_AS_ADA_HOME_OFFICE";
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
