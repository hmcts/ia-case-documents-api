package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealDecision;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Service
public class HomeOfficeAppealOutcomePersonalisation implements EmailNotificationPersonalisation {

    private final String appealOutcomeAllowedHomeOfficeTemplateId;
    private final String appealOutcomeDismissedHomeOfficeTemplateId;
    private final String iaExUiFrontendUrl;
    private final String allowedAppealHomeOfficeEmailAddress;
    private final String dismissedAppealHomeOfficeEmailAddress;
    private final CustomerServicesProvider customerServicesProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public HomeOfficeAppealOutcomePersonalisation(
        @Value("${allowedAppealHomeOfficeEmailAddress}") String allowedAppealHomeOfficeEmailAddress,
        @Value("${dismissedAppealHomeOfficeEmailAddress}") String dismissedAppealHomeOfficeEmailAddress,
        @NotNull(message = "appealOutcomeAllowedHomeOfficeTemplateId cannot be null") @Value("${govnotify.template.appealOutcomeAllowed.homeOffice.email}") String appealOutcomeAllowedHomeOfficeTemplateId,
        @NotNull(message = "appealOutcomeDismissedHomeOfficeTemplateId cannot be null") @Value("${govnotify.template.appealOutcomeDismissed.homeOffice.email}") String appealOutcomeDismissedHomeOfficeTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.appealOutcomeAllowedHomeOfficeTemplateId = appealOutcomeAllowedHomeOfficeTemplateId;
        this.appealOutcomeDismissedHomeOfficeTemplateId = appealOutcomeDismissedHomeOfficeTemplateId;
        this.allowedAppealHomeOfficeEmailAddress = allowedAppealHomeOfficeEmailAddress;
        this.dismissedAppealHomeOfficeEmailAddress = dismissedAppealHomeOfficeEmailAddress;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        return getAppealDecision(asylumCase).equals(AppealDecision.ALLOWED.getValue())
            ? appealOutcomeAllowedHomeOfficeTemplateId : appealOutcomeDismissedHomeOfficeTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {

        return Collections.singleton(
            getAppealDecision(asylumCase).equals(AppealDecision.ALLOWED.getValue())
                ? allowedAppealHomeOfficeEmailAddress : dismissedAppealHomeOfficeEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_OUTCOME_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .build();
    }

    public String getAppealDecision(AsylumCase asylumCase) {

        final AppealDecision appealOutcomeDecision = asylumCase
            .read(AsylumCaseDefinition.IS_DECISION_ALLOWED, AppealDecision.class)
            .orElseThrow(() -> new IllegalStateException("appealOutcomeDecision is not present"));

        return appealOutcomeDecision.getValue();
    }
}
