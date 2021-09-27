package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealReviewOutcome;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;


@Service
public class AppellantRequestResponseReviewPersonalisationSms implements SmsNotificationPersonalisation {

    private final String withdrawnResponseReviewDirectionTemplateId;
    private final String iaAipFrontendUrl;
    private final EmailAddressFinder emailAddressFinder;
    private final RecipientsFinder recipientsFinder;

    public AppellantRequestResponseReviewPersonalisationSms(
            @Value("${govnotify.template.responseReviewDirection.appellant.withdrawn.sms}") String withdrawnResponseReviewDirectionTemplateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            EmailAddressFinder emailAddressFinder,
            RecipientsFinder recipientsFinder
    ) {
        this.withdrawnResponseReviewDirectionTemplateId = withdrawnResponseReviewDirectionTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.emailAddressFinder = emailAddressFinder;
        this.recipientsFinder = recipientsFinder;
    }


    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isResponseReviewDecisionWithdrawn(asylumCase)
                ? withdrawnResponseReviewDirectionTemplateId : "";
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REQUEST_RESPONSE_REVIEW_AIP_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
                ImmutableMap
                        .<String, String>builder()
                        .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("Hyperlink to service", iaAipFrontendUrl)
                        .put("designated hearing centre", emailAddressFinder.getHearingCentreEmailAddress(asylumCase))
                        .build();
    }

    protected boolean isResponseReviewDecisionWithdrawn(AsylumCase asylumCase) {

        Optional<AppealReviewOutcome> appealDecisionOutcome
                = asylumCase.read(AsylumCaseDefinition.APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class);
        return
                appealDecisionOutcome.isPresent() && appealDecisionOutcome.get().toString()
                        .equals(AppealReviewOutcome.DECISION_WITHDRAWN.toString());
    }
}
