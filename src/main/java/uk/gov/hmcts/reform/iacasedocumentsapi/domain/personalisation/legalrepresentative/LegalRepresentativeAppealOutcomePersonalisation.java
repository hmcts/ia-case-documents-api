package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class LegalRepresentativeAppealOutcomePersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String appealOutcomeAllowedLegalRepresentativeTemplateId;
    private final String appealOutcomeDismissedLegalRepresentativeTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public LegalRepresentativeAppealOutcomePersonalisation(
        @NotNull(message = "appealOutcomeAllowedLegalRepresentativeTemplateId cannot be null") @Value("${govnotify.template.appealOutcomeAllowed.legalRep.email}") String appealOutcomeAllowedLegalRepresentativeTemplateId,
        @NotNull(message = "appealOutcomeDismissedLegalRepresentativeTemplateId cannot be null") @Value("${govnotify.template.appealOutcomeDismissed.legalRep.email}") String appealOutcomeDismissedLegalRepresentativeTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.appealOutcomeAllowedLegalRepresentativeTemplateId = appealOutcomeAllowedLegalRepresentativeTemplateId;
        this.appealOutcomeDismissedLegalRepresentativeTemplateId = appealOutcomeDismissedLegalRepresentativeTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        final AppealDecision appealOutcomeDecision = asylumCase
            .read(AsylumCaseDefinition.IS_DECISION_ALLOWED, AppealDecision.class)
            .orElseThrow(() -> new IllegalStateException("appealOutcomeDecision is not present"));

        return appealOutcomeDecision.getValue().equals(AppealDecision.ALLOWED.getValue())
            ? appealOutcomeAllowedLegalRepresentativeTemplateId : appealOutcomeDismissedLegalRepresentativeTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_OUTCOME_LEGAL_REPRESENTATIVE";
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
            .put("legalRepReferenceNumber", asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .build();
    }
}
