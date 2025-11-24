package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.PinInPostDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@Service
public class CaseOfficerRemoveRepresentationPersonalisation implements EmailNotificationPersonalisation {

    private final String iaAipFrontendUrl;
    private final String iaAipPathToSelfRepresentation;
    private final String beforeListingTemplateId;
    private final String afterListingTemplateId;
    private final String iaExUiFrontendUrl;
    private final AppealService appealService;
    private final EmailAddressFinder emailAddressFinder;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public CaseOfficerRemoveRepresentationPersonalisation(
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        @Value("${iaAipPathToSelfRepresentation}") String iaAipPathToSelfRepresentation,
        @NotNull(message = "removeRepresentationCaseOfficerBeforeListingTemplateId cannot be null") @Value("${govnotify.template.removeRepresentation.caseOfficer.beforeListing.email}") String beforeListingTemplateId,
        @NotNull(message = "removeRepresentationCaseOfficerBeforeListingTemplateId cannot be null") @Value("${govnotify.template.removeRepresentation.caseOfficer.afterListing.email}") String afterListingTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        AppealService appealService,
        EmailAddressFinder emailAddressFinder) {
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.iaAipPathToSelfRepresentation = iaAipPathToSelfRepresentation;
        this.beforeListingTemplateId = beforeListingTemplateId;
        this.afterListingTemplateId = afterListingTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.appealService = appealService;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return appealService.isAppealListed(asylumCase)
            ? afterListingTemplateId
            : beforeListingTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getHearingCentreEmailAddress(asylumCase));        
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REMOVE_REPRESENTATION_CASE_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        requireNonNull(asylumCase, "asylumCase must not be null");

        String linkToPiPStartPage = iaAipFrontendUrl + iaAipPathToSelfRepresentation;

        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
            .<String, String>builder()
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ccdCaseId", String.valueOf(callback.getCaseDetails().getId()))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("linkToPiPStartPage", linkToPiPStartPage);

        if (appealService.isAppealListed(asylumCase)) {
            personalizationBuilder.put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""));
        }

        PinInPostDetails pip = asylumCase.read(AsylumCaseDefinition.APPELLANT_PIN_IN_POST, PinInPostDetails.class).orElse(null);
        if (pip != null) {
            personalizationBuilder.put("securityCode", pip.getAccessCode());
            personalizationBuilder.put("validDate", defaultDateFormat((pip.getExpiryDate())));
        } else {
            personalizationBuilder.put("securityCode", "");
            personalizationBuilder.put("validDate", "");
        }

        return personalizationBuilder.build();
    }
}
