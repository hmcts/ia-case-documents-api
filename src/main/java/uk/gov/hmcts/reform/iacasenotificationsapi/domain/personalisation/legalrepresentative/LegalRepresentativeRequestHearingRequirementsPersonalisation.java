package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@Service
public class LegalRepresentativeRequestHearingRequirementsPersonalisation implements EmailNotificationPersonalisation {

    private final String legalRepRequestHearingRequirementsTemplateId;
    private final String iaExUiFrontendUrl;
    private final EmailAddressFinder emailAddressFinder;
    private final DirectionFinder directionFinder;
    private final CustomerServicesProvider customerServicesProvider;

    public LegalRepresentativeRequestHearingRequirementsPersonalisation(
        @Value("${govnotify.template.requestHearingRequirements.legalRep.email}") String legalRepRequestHearingRequirementsTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        EmailAddressFinder emailAddressFinder,
        DirectionFinder directionFinder,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.legalRepRequestHearingRequirementsTemplateId = legalRepRequestHearingRequirementsTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.emailAddressFinder = emailAddressFinder;
        this.directionFinder = directionFinder;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return legalRepRequestHearingRequirementsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getLegalRepEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_LEGAL_REPRESENTATIVE_REQUEST_HEARING_REQUIREMENTS_DIRECTION";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final Direction direction =
            directionFinder
                .findFirst(asylumCase, DirectionTag.LEGAL_REPRESENTATIVE_HEARING_REQUIREMENTS)
                .orElseThrow(() -> new IllegalStateException("case officer request hearing requirements direction is not present"));

        final String directionDueDate =
            LocalDate
                .parse(direction.getDateDue())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return
            ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .put("explanation", direction.getExplanation())
                .put("dueDate", directionDueDate)
                .build();
    }
}
