package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class CaseOfficerRequestHearingRequirementsPersonalisation implements EmailNotificationPersonalisation {
    private final String caseOfficerRequestHearingRequirementsTemplateId;
    private final String iaExUiFrontendUrl;
    private final Map<HearingCentre, String> hearingCentreEmailAddresses;
    private final CustomerServicesProvider customerServicesProvider;
    private final FeatureToggler featureToggler;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public CaseOfficerRequestHearingRequirementsPersonalisation(
            @Value("${govnotify.template.requestHearingRequirements.caseOfficer.email}") String caseOfficerRequestHearingRequirementsTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            Map<HearingCentre, String> hearingCentreEmailAddresses,
            CustomerServicesProvider customerServicesProvider,
            FeatureToggler featureToggler) {
        this.caseOfficerRequestHearingRequirementsTemplateId = caseOfficerRequestHearingRequirementsTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.hearingCentreEmailAddresses = hearingCentreEmailAddresses;
        this.customerServicesProvider = customerServicesProvider;
        this.featureToggler = featureToggler;
    }

    @Override
    public String getTemplateId() {
        return caseOfficerRequestHearingRequirementsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return featureToggler.getValue("tcw-notifications-feature", true)
                ? Collections.singleton(asylumCase
                    .read(HEARING_CENTRE, HearingCentre.class)
                    .map(centre -> Optional.ofNullable(hearingCentreEmailAddresses.get(centre))
                            .orElseThrow(() -> new IllegalStateException("Hearing centre email address not found: " + centre.toString()))
                    )
                    .orElseThrow(() -> new IllegalStateException("hearingCentre is not present")))
                : Collections.emptySet();
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CASE_OFFICER_REQUEST_HEARING_REQUIREMENTS_DIRECTION";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
                ImmutableMap
                        .<String, String>builder()
                        .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                        .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                        .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                        .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                        .put("linkToOnlineService", iaExUiFrontendUrl)
                        .build();
    }
}
