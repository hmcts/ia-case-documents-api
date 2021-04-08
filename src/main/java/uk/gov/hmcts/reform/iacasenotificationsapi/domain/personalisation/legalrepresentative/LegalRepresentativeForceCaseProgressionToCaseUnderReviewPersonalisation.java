package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;

@Service
public class LegalRepresentativeForceCaseProgressionToCaseUnderReviewPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String forceCaseProgressionToCaseUnderReviewLegalRepresentativeTemplateId;

    public LegalRepresentativeForceCaseProgressionToCaseUnderReviewPersonalisation(
        @NotNull(message = "forceCaseProgressionToCaseUnderReviewLegalRepresentativeTemplateId cannot be null") @Value("${govnotify.template.forceCaseProgression.caseBuilding.to.caseUnderReview.legalRep.email}") String forceCaseProgressionToCaseUnderReviewLegalRepresentativeTemplateId) {

        this.forceCaseProgressionToCaseUnderReviewLegalRepresentativeTemplateId = forceCaseProgressionToCaseUnderReviewLegalRepresentativeTemplateId;
    }

    @Override
    public String getTemplateId() {
        return forceCaseProgressionToCaseUnderReviewLegalRepresentativeTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_FORCE_CASE_TO_CASE_UNDER_REVIEW_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReferenceNumber", asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .build();
    }
}
