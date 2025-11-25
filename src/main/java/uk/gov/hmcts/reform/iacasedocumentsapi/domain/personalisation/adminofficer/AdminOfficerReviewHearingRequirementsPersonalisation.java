package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.adminofficer;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;


@Service
public class AdminOfficerReviewHearingRequirementsPersonalisation implements EmailNotificationPersonalisation {

    private final String reviewHearingRequirementsAdminOfficerTemplateId;
    private final String reviewReheardHearingRequirementsAdminOfficerTemplateId;
    private final String reviewHearingRequirementsAdminOfficerEmailAddress;
    private final AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public AdminOfficerReviewHearingRequirementsPersonalisation(
        @NotNull(message = "reviewHearingRequirementsAdminOfficerTemplateId cannot be null") @Value("${govnotify.template.reviewHearingRequirements.adminOfficer.email}") String reviewHearingRequirementsAdminOfficerTemplateId,
        @NotNull(message = "reviewReheardHearingRequirementsAdminOfficerTemplateId cannot be null") @Value("${govnotify.template.reviewReheardHearingRequirements.adminOfficer.email}") String reviewReheardHearingRequirementsAdminOfficerTemplateId,
        @Value("${reviewHearingRequirementsAdminOfficerEmailAddress}") String reviewHearingRequirementsAdminOfficerEmailAddress,
        AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider
    ) {
        this.reviewHearingRequirementsAdminOfficerTemplateId = reviewHearingRequirementsAdminOfficerTemplateId;
        this.reviewReheardHearingRequirementsAdminOfficerTemplateId = reviewReheardHearingRequirementsAdminOfficerTemplateId;
        this.reviewHearingRequirementsAdminOfficerEmailAddress = reviewHearingRequirementsAdminOfficerEmailAddress;
        this.adminOfficerPersonalisationProvider = adminOfficerPersonalisationProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        if ((asylumCase.read(AsylumCaseDefinition.IS_REHEARD_APPEAL_ENABLED, YesOrNo.class).equals(Optional.of(YesOrNo.YES))
             && (asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false)))) {
            return reviewReheardHearingRequirementsAdminOfficerTemplateId;
        } else {
            return reviewHearingRequirementsAdminOfficerTemplateId;
        }
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
                .putAll(adminOfficerPersonalisationProvider.getReviewedHearingRequirementsPersonalisation(asylumCase))
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .build();
    }
}
