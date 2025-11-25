package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.respondent;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;

@Service
public class RespondentForceCaseToSubmitHearingRequirementsPersonalisation implements EmailNotificationPersonalisation {

    private final String respondentForceCaseToSubmitHearingRequirementsTemplateId;
    private final String respondentForceCaseToSubmitHearingRequirementsDetentionTemplateId;
    private final String respondentEmailAddressAtRespondentReview;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;

    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public RespondentForceCaseToSubmitHearingRequirementsPersonalisation(
        @NotNull(message = "respondentForceCaseToSubmitHearingRequirementsTemplateId cannot be null")
        @Value("${govnotify.template.forceCaseProgression.respondentReview.to.submitHearingRequirements.respondent.email}") String respondentForceCaseToSubmitHearingRequirementsTemplateId,
        @Value("${govnotify.template.forceCaseProgression.respondentReview.to.submitHearingRequirements.respondent.detention.email}") String respondentForceCaseToSubmitHearingRequirementsDetentionTemplateId,
        @NotNull(message = "respondentEmailAddressAtRespondentReview cannot be null")
        @Value("${respondentEmailAddresses.respondentReviewDirection}") String respondentEmailAddressAtRespondentReview
    ) {

        this.respondentForceCaseToSubmitHearingRequirementsTemplateId = respondentForceCaseToSubmitHearingRequirementsTemplateId;
        this.respondentForceCaseToSubmitHearingRequirementsDetentionTemplateId = respondentForceCaseToSubmitHearingRequirementsDetentionTemplateId;
        this.respondentEmailAddressAtRespondentReview = respondentEmailAddressAtRespondentReview;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        if (isAppellantInDetention(asylumCase)) {
            return respondentForceCaseToSubmitHearingRequirementsDetentionTemplateId;
        } else {
            return respondentForceCaseToSubmitHearingRequirementsTemplateId;
        }
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(respondentEmailAddressAtRespondentReview);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS_RESPONDENT";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .build();
    }
}
