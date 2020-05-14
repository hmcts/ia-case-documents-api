package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class RespondentEvidenceDirectionPersonalisation implements EmailNotificationPersonalisation {

    private final String respondentEvidenceDirectionTemplateId;
    private final String respondentEvidenceDirectionEmailAddress;
    private final String iaExUiFrontendUrl;
    private final DirectionFinder directionFinder;
    private final CustomerServicesProvider customerServicesProvider;

    public RespondentEvidenceDirectionPersonalisation(
        @Value("${govnotify.template.requestRespondentEvidenceDirection.respondent.email}") String respondentEvidenceDirectionTemplateId,
        @Value("${respondentEmailAddresses.respondentEvidenceDirection}") String respondentEvidenceDirectionEmailAddress,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        DirectionFinder directionFinder,
        CustomerServicesProvider customerServicesProvider
    ) {

        this.respondentEvidenceDirectionTemplateId = respondentEvidenceDirectionTemplateId;
        this.respondentEvidenceDirectionEmailAddress = respondentEvidenceDirectionEmailAddress;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.directionFinder = directionFinder;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return respondentEvidenceDirectionTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(respondentEvidenceDirectionEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RESPONDENT_EVIDENCE_DIRECTION";
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

        return ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("explanation", direction.getExplanation())
            .put("dueDate", directionDueDate)
            .build();
    }
}
