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
public class AdminOfficerWithoutHearingRequirementsPersonalisation implements EmailNotificationPersonalisation {

    private final String withoutHearingRequirementsAdminOfficerTemplateId;
    private final String reviewHearingRequirementsAdminOfficerEmailAddress;
    private final PersonalisationProvider personalisationProvider;

    public AdminOfficerWithoutHearingRequirementsPersonalisation(
        @NotNull(message = "withoutHearingRequirementsAdminOfficerTemplateId cannot be null") @Value("${govnotify.template.withoutHearingRequirementsAdminOfficerTemplateId}") String withoutHearingRequirementsAdminOfficerTemplateId,
        @Value("${reviewHearingRequirementsAdminOfficerEmailAddress}") String reviewHearingRequirementsAdminOfficerEmailAddress,
        PersonalisationProvider personalisationProvider
    ) {
        this.withoutHearingRequirementsAdminOfficerTemplateId = withoutHearingRequirementsAdminOfficerTemplateId;
        this.reviewHearingRequirementsAdminOfficerEmailAddress = reviewHearingRequirementsAdminOfficerEmailAddress;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_WITHOUT_HEARING_REQUIREMENTS_ADMIN_OFFICER";
    }

    @Override
    public String getTemplateId() {
        return withoutHearingRequirementsAdminOfficerTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(reviewHearingRequirementsAdminOfficerEmailAddress);
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return personalisationProvider.getReviewedHearingRequirementsPersonalisation(asylumCase);
    }
}
