package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;


@Service
public class AdminOfficerReviewHearingRequirementsPersonalisation implements EmailNotificationPersonalisation {

    private final String reviewHearingRequirementsAdminOfficerTemplateId;
    private final String reviewHearingRequirementsAdminOfficerEmailAddress;

    public AdminOfficerReviewHearingRequirementsPersonalisation(
        @Value("${govnotify.template.reviewHearingRequirementsAdminOfficerTemplateId}") String reviewHearingRequirementsAdminOfficerTemplateId,
        @Value("${reviewHearingRequirementsAdminOfficerEmailAddress}") String reviewHearingRequirementsAdminOfficerEmailAddress
    ) {
        this.reviewHearingRequirementsAdminOfficerTemplateId = reviewHearingRequirementsAdminOfficerTemplateId;
        this.reviewHearingRequirementsAdminOfficerEmailAddress = reviewHearingRequirementsAdminOfficerEmailAddress;
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

        return
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .build();
    }
}
