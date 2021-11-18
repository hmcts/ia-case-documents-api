package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@Service
public class CaseOfficerRemoveRepresentationPersonalisation implements EmailNotificationPersonalisation {

    private final String removeRepresentationCaseOfficerBeforeListingTemplateId;
    private final String removeRepresentationCaseOfficerAfterListingTemplateId;
    private final String iaExUiFrontendUrl;
    private final AppealService appealService;
    private final EmailAddressFinder emailAddressFinder;

    public CaseOfficerRemoveRepresentationPersonalisation(
            @NotNull(message = "removeRepresentationCaseOfficerBeforeListingTemplateId cannot be null") @Value("${govnotify.template.removeRepresentation.caseOfficer.beforeListing.email}") String removeRepresentationCaseOfficerBeforeListingTemplateId,
            @NotNull(message = "removeRepresentationCaseOfficerAfterListingTemplateId cannot be null") @Value("${govnotify.template.removeRepresentation.caseOfficer.afterListing.email}") String removeRepresentationCaseOfficerAfterListingTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            AppealService appealService,
            EmailAddressFinder emailAddressFinder) {
        this.removeRepresentationCaseOfficerBeforeListingTemplateId = removeRepresentationCaseOfficerBeforeListingTemplateId;
        this.removeRepresentationCaseOfficerAfterListingTemplateId = removeRepresentationCaseOfficerAfterListingTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.appealService = appealService;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return appealService.isAppealListed(asylumCase)
            ? removeRepresentationCaseOfficerAfterListingTemplateId
            : removeRepresentationCaseOfficerBeforeListingTemplateId;
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
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
    }
}
