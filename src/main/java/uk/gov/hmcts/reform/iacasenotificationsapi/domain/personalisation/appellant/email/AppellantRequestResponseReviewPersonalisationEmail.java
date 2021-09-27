package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;


@Service
public class AppellantRequestResponseReviewPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String withdrawnResponseReviewDirectionTemplateId;
    private final String maintainedResponseReviewDirectionTemplateId;
    private final String iaAipFrontendUrl;
    private final EmailAddressFinder emailAddressFinder;
    private final RecipientsFinder recipientsFinder;
    private final CustomerServicesProvider customerServicesProvider;
    private final DirectionFinder directionFinder;

    public AppellantRequestResponseReviewPersonalisationEmail(
            @Value("${govnotify.template.responseReviewDirection.appellant.withdrawn.email}") String withdrawnResponseReviewDirectionTemplateId,
            @Value("${govnotify.template.responseReviewDirection.appellant.maintained.email}") String maintainedResponseReviewDirectionTemplateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            EmailAddressFinder emailAddressFinder,
            RecipientsFinder recipientsFinder,
            CustomerServicesProvider customerServicesProvider,
            DirectionFinder directionFinder
    ) {
        this.withdrawnResponseReviewDirectionTemplateId = withdrawnResponseReviewDirectionTemplateId;
        this.maintainedResponseReviewDirectionTemplateId = maintainedResponseReviewDirectionTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.emailAddressFinder = emailAddressFinder;
        this.recipientsFinder = recipientsFinder;
        this.customerServicesProvider = customerServicesProvider;
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
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REQUEST_RESPONSE_REVIEW_AIP_EMAIL";
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
                        .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                        .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                        .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                        .put("Hyperlink to service", iaAipFrontendUrl)
                        .put("designated hearing centre", emailAddressFinder.getHearingCentreEmailAddress(asylumCase))
                        .put("dueDate", directionDueDate)
                        .build();
    }

}
