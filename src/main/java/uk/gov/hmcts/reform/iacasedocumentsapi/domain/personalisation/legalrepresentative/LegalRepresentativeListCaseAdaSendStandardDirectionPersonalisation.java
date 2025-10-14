package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@Service
public class LegalRepresentativeListCaseAdaSendStandardDirectionPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {
    private final String legalRepresentativeCaseListedAdaSendDirectionTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final HearingDetailsFinder hearingDetailsFinder;
    private final DirectionFinder directionFinder;


    public LegalRepresentativeListCaseAdaSendStandardDirectionPersonalisation(
        @Value("${govnotify.template.adaCaseListedSendDirection.legalRep.email}") String legalRepresentativeCaseListedAdaSendDirectionTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        CustomerServicesProvider customerServicesProvider,
        HearingDetailsFinder hearingDetailsFinder,
        DirectionFinder directionFinder
    ) {
        this.legalRepresentativeCaseListedAdaSendDirectionTemplateId = legalRepresentativeCaseListedAdaSendDirectionTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.hearingDetailsFinder = hearingDetailsFinder;
        this.directionFinder = directionFinder;
    }

    @Override
    public String getTemplateId() {
        return legalRepresentativeCaseListedAdaSendDirectionTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CASE_LISTED_SEND_DIRECTION_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        Builder<String, String> listCaseFields;
        final Direction direction =
                directionFinder
                        .findFirst(asylumCase, DirectionTag.ADA_LIST_CASE)
                        .orElseThrow(() -> new IllegalStateException("LR List ADA Case direction is not present"));

        String notificationBody = direction.getExplanation()
                                  + "\n\nYou must complete this direction by: "
                                  + LocalDate.parse(direction.getDateDue()).format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));

        listCaseFields = ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("explanation", notificationBody)
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .put("hearingCentreAddress", hearingDetailsFinder.getHearingCentreLocation(asylumCase));

        PersonalisationProvider.buildHearingRequirementsFields(asylumCase, listCaseFields);

        return listCaseFields.build();
    }
}
