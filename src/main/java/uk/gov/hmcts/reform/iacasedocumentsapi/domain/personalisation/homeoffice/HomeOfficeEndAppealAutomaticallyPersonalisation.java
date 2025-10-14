package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
public class HomeOfficeEndAppealAutomaticallyPersonalisation implements EmailNotificationPersonalisation {

    private final String endAppealAutomaticallyHomeOfficeTemplateId;
    private final String endAppealHomeOfficeEmailAddress;
    private final CustomerServicesProvider customerServicesProvider;
    private final String iaExUiFrontendUrl;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public HomeOfficeEndAppealAutomaticallyPersonalisation(
        @Value("${endAppealHomeOfficeEmailAddress}") String endAppealHomeOfficeEmailAddress,
        @NotNull(message = "appealEndedAutomaticallyHomeOfficeTemplateId cannot be null") @Value("${govnotify.template.endAppealAutomatically.homeOffice.email}") String endAppealAutomaticallyHomeOfficeTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.endAppealHomeOfficeEmailAddress = endAppealHomeOfficeEmailAddress;
        this.endAppealAutomaticallyHomeOfficeTemplateId = endAppealAutomaticallyHomeOfficeTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return endAppealAutomaticallyHomeOfficeTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(endAppealHomeOfficeEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_ENDED_AUTOMATICALLY_HOME_OFFICE";
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
            .put("endAppealDate", asylumCase.read(AsylumCaseDefinition.END_APPEAL_DATE, String.class)
                .map(date -> LocalDate.parse(date).format(DateTimeFormatter.ofPattern("d MMM yyyy")))
                .orElse(""))
            .build();
    }

}
