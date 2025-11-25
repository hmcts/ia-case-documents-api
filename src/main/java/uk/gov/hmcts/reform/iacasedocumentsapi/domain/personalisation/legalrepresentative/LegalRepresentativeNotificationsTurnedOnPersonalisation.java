package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_EMAIL_EJP;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAppealListed;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@Service
public class LegalRepresentativeNotificationsTurnedOnPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepresentativeTransferredToFirstTierAfterListingTemplateId;
    private final String legalRepresentativeTransferredToFirstTierBeforeListingTemplateId;
    private final String iaExUiFrontendUrl;
    private final PersonalisationProvider personalisationProvider;


    public LegalRepresentativeNotificationsTurnedOnPersonalisation(
        @NotNull(message = "legalRepresentativeTransferredToFirstTierBeforeListingTemplateId cannot be null")
        @Value("${govnotify.template.turnOnNotifications.legalRep.beforeListing.email}") String legalRepresentativeTransferredToFirstTierBeforeListingTemplateId,
        @NotNull(message = "legalRepresentativeTransferredToFirstTierAfterListingTemplateId cannot be null")
        @Value("${govnotify.template.turnOnNotifications.legalRep.afterListing.email}") String legalRepresentativeTransferredToFirstTierAfterListingTemplateId,
        PersonalisationProvider personalisationProvider,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl

    ) {
        this.legalRepresentativeTransferredToFirstTierAfterListingTemplateId = legalRepresentativeTransferredToFirstTierAfterListingTemplateId;
        this.legalRepresentativeTransferredToFirstTierBeforeListingTemplateId = legalRepresentativeTransferredToFirstTierBeforeListingTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAppealListed(asylumCase)
            ? legalRepresentativeTransferredToFirstTierAfterListingTemplateId : legalRepresentativeTransferredToFirstTierBeforeListingTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(asylumCase
            .read(LEGAL_REP_EMAIL_EJP, String.class)
            .orElseThrow(() -> new IllegalStateException("legalRepresentativeEmailAddress is not present")));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_NOTIFICATIONS_TURNED_ON";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .putAll(personalisationProvider.getRespondentHeaderPersonalisation(asylumCase))
            .put("legalRepReferenceNumberEjp", asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_EJP, String.class).orElse(""))
            .put("ccdReferenceNumberForDisplay", asylumCase.read(AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class).orElse(""))
            .put("dateOfBirth", defaultDateFormat(asylumCase.read(AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH, String.class).orElse("")))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .build();
    }
}

