package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;

@Component
public class AdminOfficerRemissionDecisionPartiallyApprovedPersonalisation implements EmailNotificationPersonalisation {

    private final String partiallyApprovedTemplateId;
    private final String feesAdminOfficerEmailAddress;

    private final AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    public AdminOfficerRemissionDecisionPartiallyApprovedPersonalisation(
        AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider,
        @Value("${govnotify.template.remissionDecision.adminOfficer.partiallyApproved.email}") String partiallyApprovedTemplateId,
        @Value("${feesAdminOfficerEmailAddress}") String feesAdminOfficerEmailAddress) {
        this.adminOfficerPersonalisationProvider = adminOfficerPersonalisationProvider;
        this.partiallyApprovedTemplateId = partiallyApprovedTemplateId;
        this.feesAdminOfficerEmailAddress = feesAdminOfficerEmailAddress;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REMISSION_DECISION_PARTIALLY_APPROVED_ADMIN_OFFICER";
    }

    @Override
    public String getTemplateId() {
        return partiallyApprovedTemplateId;
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
