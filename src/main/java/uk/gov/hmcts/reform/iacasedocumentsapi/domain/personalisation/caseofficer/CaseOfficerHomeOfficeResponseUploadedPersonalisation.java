package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.caseofficer;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;

@Service
public class CaseOfficerHomeOfficeResponseUploadedPersonalisation implements EmailNotificationPersonalisation {

    private final String homeOfficeResponseUploadedTemplateId;
    private final String iaExUiFrontendUrl;
    private final Map<HearingCentre, String> hearingCentreEmailAddresses;
    private final FeatureToggler featureToggler;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public CaseOfficerHomeOfficeResponseUploadedPersonalisation(
            @Value("${govnotify.template.homeOfficeResponseUploaded.caseOfficer.email}") String homeOfficeResponseUploadedTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            Map<HearingCentre, String> hearingCentreEmailAddresses,
            FeatureToggler featureToggler) {
        this.homeOfficeResponseUploadedTemplateId = homeOfficeResponseUploadedTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.hearingCentreEmailAddresses = hearingCentreEmailAddresses;
        this.featureToggler = featureToggler;
    }

    @Override
    public String getTemplateId() {
        return homeOfficeResponseUploadedTemplateId;
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
        return caseId + "_UPLOADED_HO_RESPONSE_CASE_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
    }
}
