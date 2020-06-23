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


@Service
public class AdminOfficerReListCasePersonalisation implements EmailNotificationPersonalisation {

    private final String reListCaseAdminOfficerTemplateId;
    private final String reviewHearingRequirementsAdminOfficerEmailAddress;
    private final AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    public AdminOfficerReListCasePersonalisation(
        @NotNull(message = "reListCaseAdminOfficerTemplateId cannot be null")
        @Value("${govnotify.template.reListCase.adminOfficer.email}") String reListCaseAdminOfficerTemplateId,
        @Value("${reviewHearingRequirementsAdminOfficerEmailAddress}")
            String reviewHearingRequirementsAdminOfficerEmailAddress,
        AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider
    ) {
        this.reListCaseAdminOfficerTemplateId = reListCaseAdminOfficerTemplateId;
        this.reviewHearingRequirementsAdminOfficerEmailAddress = reviewHearingRequirementsAdminOfficerEmailAddress;
        this.adminOfficerPersonalisationProvider = adminOfficerPersonalisationProvider;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RE_LIST_CASE_ADMIN_OFFICER";
    }

    @Override
    public String getTemplateId() {
        return reListCaseAdminOfficerTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(reviewHearingRequirementsAdminOfficerEmailAddress);
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return adminOfficerPersonalisationProvider.getChangeToHearingRequirementsPersonalisation(asylumCase);

    }
}
