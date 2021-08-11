package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;

@Service
public class AppellantDecideAnApplicationRefusedPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String decideAnApplicationBeforeListingAppellantEmailTemplateId;
    private final String decideAnApplicationAfterListingAppellantEmailTemplateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;
    private final MakeAnApplicationService makeAnApplicationService;
    private final AppealService appealService;

    public AppellantDecideAnApplicationRefusedPersonalisationEmail(
            @Value("${govnotify.template.decideAnApplication.refused.applicant.appellant.beforeListing.email}") String decideAnApplicationBeforeListingAppellantEmailTemplateId,
            @Value("${govnotify.template.decideAnApplication.refused.applicant.appellant.afterListing.email}") String decideAnApplicationAfterListingAppellantEmailTemplateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            RecipientsFinder recipientsFinder,
            MakeAnApplicationService makeAnApplicationService,
            AppealService appealService) {
        this.decideAnApplicationBeforeListingAppellantEmailTemplateId = decideAnApplicationBeforeListingAppellantEmailTemplateId;
        this.decideAnApplicationAfterListingAppellantEmailTemplateId = decideAnApplicationAfterListingAppellantEmailTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
        this.makeAnApplicationService = makeAnApplicationService;
        this.appealService = appealService;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        return
                appealService.isAppealListed(asylumCase)
                        ? decideAnApplicationAfterListingAppellantEmailTemplateId
                        : decideAnApplicationBeforeListingAppellantEmailTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);

    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_DECIDE_AN_APPLICATION_APPELLANT_REFUSED_AIP_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("ariaListingReference", asylumCase.read(AsylumCaseDefinition.ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("HO Ref Number", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("Given names", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("Family name", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("applicationType", makeAnApplicationService.getMakeAnApplicationTypeName(asylumCase))
                .put("Hyperlink to service", iaAipFrontendUrl)
                .build();
    }
}
