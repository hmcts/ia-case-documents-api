package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class AppellantHearingBundleReadyPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String appellantHearingBundleReadyTemplateId;
    private final RecipientsFinder recipientsFinder;
    private final CustomerServicesProvider customerServicesProvider;
    private final String iaAipFrontendUrl;
    private final FeatureToggler featureToggler;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public AppellantHearingBundleReadyPersonalisationEmail(
        @Value("${govnotify.template.hearingBundleReady.appellant.email}") String appellantHearingBundleReadyTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        CustomerServicesProvider customerServicesProvider,
        RecipientsFinder recipientsFinder,
        FeatureToggler featureToggler
    ) {
        this.appellantHearingBundleReadyTemplateId = appellantHearingBundleReadyTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.recipientsFinder = recipientsFinder;
        this.featureToggler = featureToggler;
    }

    @Override
    public String getTemplateId() {
        return appellantHearingBundleReadyTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return featureToggler.getValue("aip-hearing-bundle-feature", false)
            ? recipientsFinder.findAll(asylumCase, NotificationType.EMAIL)
            : Collections.emptySet();
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_HEARING_BUNDLE_IS_READY_APPELLANT_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ccdReferenceNumber", asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class).orElse(""))
            .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("Hyperlink to service", iaAipFrontendUrl)
            .build();
    }
}
