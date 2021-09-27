package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;


@Service
public class AppellantRequestResponseReviewPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String withdrawnResponseReviewDirectionTemplateId;
    private final String iaAipFrontendUrl;
    private final EmailAddressFinder emailAddressFinder;
    private final RecipientsFinder recipientsFinder;
    private final CustomerServicesProvider customerServicesProvider;

    public AppellantRequestResponseReviewPersonalisationEmail(
            @Value("${govnotify.template.responseReviewDirection.appellant.withdrawn.email}") String withdrawnResponseReviewDirectionTemplateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            EmailAddressFinder emailAddressFinder,
            RecipientsFinder recipientsFinder,
            CustomerServicesProvider customerServicesProvider
    ) {
        this.withdrawnResponseReviewDirectionTemplateId = withdrawnResponseReviewDirectionTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.emailAddressFinder = emailAddressFinder;
        this.recipientsFinder = recipientsFinder;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isResponseReviewDecisionWithdrawn(asylumCase)
                ? withdrawnResponseReviewDirectionTemplateId : "";
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
