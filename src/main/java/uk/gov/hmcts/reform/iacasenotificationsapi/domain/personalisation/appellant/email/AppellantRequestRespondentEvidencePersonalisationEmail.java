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
public class AppellantRequestRespondentEvidencePersonalisationEmail implements EmailNotificationPersonalisation {

    private final String requestRespondentEvidenceDirectionAppellantEmailTemplateId;
    private final String iaAipFrontendUrl;
    private final DirectionFinder directionFinder;
    private final RecipientsFinder recipientsFinder;

    public AppellantRequestRespondentEvidencePersonalisationEmail(
        @Value("${govnotify.template.requestRespondentEvidenceDirectionAppellant.email}") String requestRespondentEvidenceDirectionAppellantEmailTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        DirectionFinder directionFinder,
        RecipientsFinder recipientsFinder
    ) {
        this.requestRespondentEvidenceDirectionAppellantEmailTemplateId = requestRespondentEvidenceDirectionAppellantEmailTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.directionFinder = directionFinder;
        this.recipientsFinder = recipientsFinder;

    }

    @Override
    public String getTemplateId() {
        return requestRespondentEvidenceDirectionAppellantEmailTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REQUEST_RESPONDENT_EVIDENCE_DIRECTION_APPELLANT_AIP_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final Direction direction =
            directionFinder
                .findFirst(asylumCase, DirectionTag.RESPONDENT_EVIDENCE)
                .orElseThrow(() -> new IllegalStateException("direction '" + DirectionTag.RESPONDENT_EVIDENCE + "' is not present"));

        final String directionDueDate =
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
                .put("direction due date", directionDueDate)
                .put("Hyperlink to service", iaAipFrontendUrl)
                .build();
    }
}
