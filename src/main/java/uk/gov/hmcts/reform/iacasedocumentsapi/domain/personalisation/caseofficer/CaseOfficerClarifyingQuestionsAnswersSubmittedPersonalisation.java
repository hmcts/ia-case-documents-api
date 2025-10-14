package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.caseofficer;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;

@Service
public class CaseOfficerClarifyingQuestionsAnswersSubmittedPersonalisation implements EmailNotificationPersonalisation {

    private final String submitClarifyingQuestionAnswersCaseOfficerSmsTemplateId;
    private final String iaExUiFrontendUrl;
    private final EmailAddressFinder emailAddressFinder;
    private final FeatureToggler featureToggler;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public CaseOfficerClarifyingQuestionsAnswersSubmittedPersonalisation(
            @NotNull(message = "submitClarifyingQuestionAnswersCaseOfficerSmsTemplateId cannot be null") @Value("${govnotify.template.submitClarifyingQuestionAnswers.caseOfficer.email}") String submitClarifyingQuestionAnswersCaseOfficerSmsTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            EmailAddressFinder emailAddressFinder,
            FeatureToggler featureToggler) {
        this.submitClarifyingQuestionAnswersCaseOfficerSmsTemplateId = submitClarifyingQuestionAnswersCaseOfficerSmsTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.emailAddressFinder = emailAddressFinder;
        this.featureToggler = featureToggler;
    }

    @Override
    public String getTemplateId() {
        return submitClarifyingQuestionAnswersCaseOfficerSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return featureToggler.getValue("tcw-notifications-feature", true)
                ? Collections.singleton(emailAddressFinder.getHearingCentreEmailAddress(asylumCase))
                : Collections.emptySet();
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CLARIFYING_QUESTION_ANSWERS_SUBMITTED_CASE_OFFICER_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase cannot be null");

        return
            ImmutableMap
                .<String, String>builder()
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .put("Appeal Ref Number", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("Appellant Given names", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("Appellant Family name", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("Hyperlink to service", iaExUiFrontendUrl)
                .build();
    }
}
