package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.TimeExtensionStatus;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.TimeExtensionFinder;

@Service
public class AppellantReviewTimeExtensionRefusedPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String reviewTimeExtensionRefusedAppellantEmailTemplateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;
    private final TimeExtensionFinder timeExtensionFinder;


    public AppellantReviewTimeExtensionRefusedPersonalisationEmail(
        @Value("${govnotify.template.reviewTimeExtensionRefused.appellant.email}") String reviewTimeExtensionRefusedAppellantEmailTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        RecipientsFinder recipientsFinder,
        TimeExtensionFinder timeExtensionFinder
    ) {
        this.reviewTimeExtensionRefusedAppellantEmailTemplateId = reviewTimeExtensionRefusedAppellantEmailTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
        this.timeExtensionFinder = timeExtensionFinder;
    }

    @Override
    public String getTemplateId() {
        return reviewTimeExtensionRefusedAppellantEmailTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REVIEW_TIME_EXTENSION_REFUSED_APPELLANT_AIP_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");
        State currentState = callback.getCaseDetails().getState();
        requireNonNull(currentState, "currentState must not be null");
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        requireNonNull(asylumCase, "asylumCase must not be null");
        final IdValue<TimeExtension> timeExtensionIdValue = timeExtensionFinder.findCurrentTimeExtension(currentState, TimeExtensionStatus.REFUSED, asylumCase);
        final String nextActionText = timeExtensionFinder.findNextActionText(currentState);

        final String dueDate =
            LocalDate
                .parse(timeExtensionIdValue.getValue().getDecisionOutcomeDate())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("HO Ref Number", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("Given names", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("Family name", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("decision reason", timeExtensionIdValue.getValue().getDecisionReason())
                .put("Next action text", nextActionText)
                .put("due date", dueDate)
                .put("Hyperlink to service", iaAipFrontendUrl)
                .build();
    }
}
