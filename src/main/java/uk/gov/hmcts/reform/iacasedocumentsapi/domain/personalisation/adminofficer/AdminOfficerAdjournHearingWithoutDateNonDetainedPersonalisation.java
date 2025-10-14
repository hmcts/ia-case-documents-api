package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.adminofficer;

import static java.util.Objects.requireNonNull;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;

@Service
public class AdminOfficerAdjournHearingWithoutDateNonDetainedPersonalisation implements EmailNotificationPersonalisation {

    private final String adjournHearingWithoutDateAdminOfficerTemplateId;
    private final String reviewHearingRequirementsAdminOfficerEmailAddress;
    private final AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    public AdminOfficerAdjournHearingWithoutDateNonDetainedPersonalisation(
        @NotNull(message = "adjournHearingWithoutDateAdminOfficerTemplateId cannot be null") @Value("${govnotify.template.adjournHearingWithoutDate.nonDetained.admin.email}") String adjournHearingWithoutDateAdminOfficerTemplateId,
        @Value("${reviewHearingRequirementsAdminOfficerEmailAddress}") String reviewHearingRequirementsAdminOfficerEmailAddress,
        AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider
    ) {
        this.adjournHearingWithoutDateAdminOfficerTemplateId = adjournHearingWithoutDateAdminOfficerTemplateId;
        this.reviewHearingRequirementsAdminOfficerEmailAddress = reviewHearingRequirementsAdminOfficerEmailAddress;
        this.adminOfficerPersonalisationProvider = adminOfficerPersonalisationProvider;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_ADJOURN_HEARING_WITHOUT_DATE_ADMIN_OFFICER";
    }

    @Override
    public String getTemplateId() {
        return adjournHearingWithoutDateAdminOfficerTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(reviewHearingRequirementsAdminOfficerEmailAddress);
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                .putAll(adminOfficerPersonalisationProvider.getDefaultPersonalisation(asylumCase))
                .build();
    }
}
