package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;

@Service
public class AppellantMakeAnApplicationPersonalisationEmail implements EmailNotificationPersonalisation {

    private static final String ROLE_CITIZEN = "citizen";
    private final String makeAnApplicationBeforeListingAppellantEmailTemplateId;
    private final String makeAnApplicationAfterListingAppellantEmailTemplateId;
    private final String otherPartyMakeAnApplicationBeforeListingAppellantEmailTemplateId;
    private final String otherPartyMakeAnApplicationAfterListingAppellantEmailTemplateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;
    private final AppealService appealService;
    private final MakeAnApplicationService makeAnApplicationService;
    private final UserDetailsProvider userDetailsProvider;

    public AppellantMakeAnApplicationPersonalisationEmail(
            @Value("${govnotify.template.makeAnApplication.beforeListing.appellant.email}") String makeAnApplicationBeforeListingAppellantEmailTemplateId,
            @Value("${govnotify.template.makeAnApplication.afterListing.appellant.email}") String makeAnApplicationAfterListingAppellantEmailTemplateId,
            @Value("${govnotify.template.makeAnApplication.beforeListing.otherParty.appellant.email}") String otherPartyMakeAnApplicationBeforeListingAppellantEmailTemplateId,
            @Value("${govnotify.template.makeAnApplication.afterListing.otherParty.appellant.email}") String otherPartyMakeAnApplicationAfterListingAppellantEmailTemplateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            RecipientsFinder recipientsFinder,
            AppealService appealService,
            MakeAnApplicationService makeAnApplicationService,
            UserDetailsProvider userDetailsProvider) {
        this.makeAnApplicationBeforeListingAppellantEmailTemplateId = makeAnApplicationBeforeListingAppellantEmailTemplateId;
        this.makeAnApplicationAfterListingAppellantEmailTemplateId = makeAnApplicationAfterListingAppellantEmailTemplateId;
        this.otherPartyMakeAnApplicationBeforeListingAppellantEmailTemplateId = otherPartyMakeAnApplicationBeforeListingAppellantEmailTemplateId;
        this.otherPartyMakeAnApplicationAfterListingAppellantEmailTemplateId = otherPartyMakeAnApplicationAfterListingAppellantEmailTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
        this.appealService = appealService;
        this.makeAnApplicationService = makeAnApplicationService;
        this.userDetailsProvider = userDetailsProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        boolean isCitizenUser = hasRole(ROLE_CITIZEN);
        if (appealService.isAppealListed(asylumCase)) {
            return isCitizenUser
                    ? makeAnApplicationAfterListingAppellantEmailTemplateId
                    : otherPartyMakeAnApplicationAfterListingAppellantEmailTemplateId;
        } else {
            return isCitizenUser
                    ? makeAnApplicationBeforeListingAppellantEmailTemplateId
                    : otherPartyMakeAnApplicationBeforeListingAppellantEmailTemplateId;
        }
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_MAKE_AN_APPLICATION_APPELLANT_AIP_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        String applicationType = makeAnApplicationService.getMakeAnApplication(asylumCase, false)
            .map(application -> !hasRole(ROLE_CITIZEN)
                ? makeAnApplicationService.mapApplicationTypeToPhrase(application)
                : application.getType())
            .orElse("");

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("HO Ref Number", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("Given names", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("Family name", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("applicationType", applicationType)
                .put("Hyperlink to service", iaAipFrontendUrl)
                .build();
    }

    private boolean hasRole(String roleName) {
        return userDetailsProvider
                .getUserDetails()
                .getRoles()
                .contains(roleName);
    }
}
