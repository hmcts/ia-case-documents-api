package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@Service
public class AppellantRequestReasonsForAppealPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String submitReasonForAppealEmailTemplateId;
    private final String iaAipFrontendUrl;
    private final DirectionFinder directionFinder;
    private final RecipientsFinder recipientsFinder;

    public AppellantRequestReasonsForAppealPersonalisationEmail(
        @Value("${govnotify.template.requestReasonsForAppeal.appellant.email}") String submitReasonForAppealEmailTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        DirectionFinder directionFinder,
        RecipientsFinder recipientsFinder
    ) {
        this.submitReasonForAppealEmailTemplateId = submitReasonForAppealEmailTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.directionFinder = directionFinder;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId() {
        return submitReasonForAppealEmailTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);

    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REQUEST_REASONS_FOR_APPEAL_APPELLANT_AIP_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final Direction direction =
            directionFinder
                .findFirst(asylumCase, DirectionTag.REQUEST_REASONS_FOR_APPEAL)
                .orElseThrow(() -> new IllegalStateException("direction '" + DirectionTag.REQUEST_REASONS_FOR_APPEAL + "' is not present"));

        final String dueDate =
            LocalDate
                .parse(direction.getDateDue())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("HO Ref Number", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("Given names", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("Family name", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("Hyperlink to service", iaAipFrontendUrl)
                .put("due date", dueDate)
                .build();
    }
}
