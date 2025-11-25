package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

import javax.validation.constraints.NotNull;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

@Service
public class LegalRepresentativeForceCaseToSubmitHearingRequirementsDetentionPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepForceCaseToSubmitHearingRequirementsDetentionTemplateId;
    private final SystemDateProvider systemDateProvider;
    private final int daysAfterRequestingHearingRequirements;
    private final String iaExUiFrontendUrl;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;

    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public LegalRepresentativeForceCaseToSubmitHearingRequirementsDetentionPersonalisation(
        @NotNull(message = "forceCaseProgressionToCaseUnderReviewLegalRepresentativeTemplateId cannot be null")
        @Value("${govnotify.template.forceCaseProgression.respondentReview.to.submitHearingRequirements.legalRep.detention.email}") String legalRepForceCaseToSubmitHearingRequirementsDetentionTemplateId,
        SystemDateProvider systemDateProvider,
        @Value("${legalRepDaysToWait.afterRequestingHearingRequirements}") int daysAfterRequestingHearingRequirements,
        @Value("${iaExUiFrontendUrl}")
            String iaExUiFrontendUrl
    ) {
        this.legalRepForceCaseToSubmitHearingRequirementsDetentionTemplateId = legalRepForceCaseToSubmitHearingRequirementsDetentionTemplateId;
        this.systemDateProvider = systemDateProvider;
        this.daysAfterRequestingHearingRequirements = daysAfterRequestingHearingRequirements;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
    }

    @Override
    public String getTemplateId() {
        return legalRepForceCaseToSubmitHearingRequirementsDetentionTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final String dueDate = systemDateProvider.dueDate(daysAfterRequestingHearingRequirements);

        return ImmutableMap
            .<String, String>builder()
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("dueDate", dueDate)
            .build();
    }
}
