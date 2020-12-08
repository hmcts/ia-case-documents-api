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
public class AdminOfficerAppealRemissionApprovedPersonalisation implements EmailNotificationPersonalisation {

    private final String caseAdminOfficerAppealRemissionApprovedTemplateId;
    private final String feesAdminOfficerEmailAddress;
    private final AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    public AdminOfficerAppealRemissionApprovedPersonalisation(
        @NotNull(message = "caseAdminOfficerAppealRemissionApprovedTemplateId cannot be null")
        @Value("${govnotify.template.remissionDecision.adminOfficer.approved.email}")
            String caseAdminOfficerAppealRemissionApprovedTemplateId,
        @Value("${feesAdminOfficerEmailAddress}")
            String feesAdminOfficerEmailAddress,
        AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider
    ) {
        this.caseAdminOfficerAppealRemissionApprovedTemplateId = caseAdminOfficerAppealRemissionApprovedTemplateId;
        this.feesAdminOfficerEmailAddress = feesAdminOfficerEmailAddress;
        this.adminOfficerPersonalisationProvider = adminOfficerPersonalisationProvider;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REMISSION_DECISION_APPROVED_ADMIN_OFFICER";
    }

    @Override
    public String getTemplateId() {
        return caseAdminOfficerAppealRemissionApprovedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(feesAdminOfficerEmailAddress);
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return adminOfficerPersonalisationProvider.getDefaultPersonalisation(asylumCase);

    }
}
