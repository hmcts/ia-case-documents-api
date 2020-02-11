package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;


@Service
public class AdminOfficerReviewHearingRequirementsPersonalisation implements EmailNotificationPersonalisation {

    private final String reviewHearingRequirementsAdminOfficerTemplateId;
    private final String reviewHearingRequirementsAdminOfficerEmailAddress;
    private final PersonalisationProvider personalisationProvider;

    public AdminOfficerReviewHearingRequirementsPersonalisation(
        @NotNull(message = "reviewHearingRequirementsAdminOfficerTemplateId cannot be null") @Value("${govnotify.template.reviewHearingRequirementsAdminOfficerTemplateId}") String reviewHearingRequirementsAdminOfficerTemplateId,
        @Value("${reviewHearingRequirementsAdminOfficerEmailAddress}") String reviewHearingRequirementsAdminOfficerEmailAddress,
        PersonalisationProvider personalisationProvider
    ) {
        this.reviewHearingRequirementsAdminOfficerTemplateId = reviewHearingRequirementsAdminOfficerTemplateId;
        this.reviewHearingRequirementsAdminOfficerEmailAddress = reviewHearingRequirementsAdminOfficerEmailAddress;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId() {
        return reviewHearingRequirementsAdminOfficerTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(reviewHearingRequirementsAdminOfficerEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REVIEW_HEARING_REQUIREMENTS_ADMIN_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return personalisationProvider.getReviewedHearingRequirementsPersonalisation(asylumCase);
    }
}
