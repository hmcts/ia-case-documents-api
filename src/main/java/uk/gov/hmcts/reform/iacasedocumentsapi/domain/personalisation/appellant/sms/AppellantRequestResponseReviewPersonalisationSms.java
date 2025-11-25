package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.EmailAddressFinder;


@Service
public class AppellantRequestResponseReviewPersonalisationSms implements SmsNotificationPersonalisation {

    private final String withdrawnResponseReviewDirectionTemplateId;
    private final String maintainedResponseReviewDirectionTemplateId;
    private final String iaAipFrontendUrl;
    private final EmailAddressFinder emailAddressFinder;
    private final RecipientsFinder recipientsFinder;
    private final DirectionFinder directionFinder;

    public AppellantRequestResponseReviewPersonalisationSms(
            @Value("${govnotify.template.responseReviewDirection.appellant.withdrawn.sms}") String withdrawnResponseReviewDirectionTemplateId,
            @Value("${govnotify.template.responseReviewDirection.appellant.maintained.sms}") String maintainedResponseReviewDirectionTemplateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            EmailAddressFinder emailAddressFinder,
            RecipientsFinder recipientsFinder,
            DirectionFinder directionFinder
    ) {
        this.withdrawnResponseReviewDirectionTemplateId = withdrawnResponseReviewDirectionTemplateId;
        this.maintainedResponseReviewDirectionTemplateId = maintainedResponseReviewDirectionTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.emailAddressFinder = emailAddressFinder;
        this.recipientsFinder = recipientsFinder;
        this.directionFinder = directionFinder;
    }


    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        AppealReviewOutcome appealReviewOutcome
                = asylumCase.read(AsylumCaseDefinition.APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class)
                .orElseThrow(() -> new IllegalArgumentException("AppealReviewOutcome not present"));

        return
                appealReviewOutcome.toString().equals(AppealReviewOutcome.DECISION_WITHDRAWN.toString())
                        ? withdrawnResponseReviewDirectionTemplateId
                        : maintainedResponseReviewDirectionTemplateId;
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

        final Direction direction =
                directionFinder
                        .findFirst(asylumCase, DirectionTag.REQUEST_RESPONSE_REVIEW)
                        .orElseThrow(() -> new IllegalStateException("direction '" + DirectionTag.REQUEST_RESPONSE_REVIEW + "' is not present"));

        final String directionDueDate =
                LocalDate
                        .parse(direction.getDateDue())
                        .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return
                ImmutableMap
                        .<String, String>builder()
                        .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("Hyperlink to service", iaAipFrontendUrl)
                        .put("designated hearing centre", emailAddressFinder.getHearingCentreEmailAddress(asylumCase))
                        .put("dueDate", directionDueDate)
                        .build();
    }
}
