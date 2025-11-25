package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.respondent;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HomeOfficeEmailFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;


@Service
public class RespondentTurnOnNotificationsPersonalisation implements EmailNotificationPersonalisation {

    private final String homeOfficeTurnOnNotificationsBeforeListingTemplateId;
    private final String homeOfficeTurnOnNotificationsAfterListingTemplateId;
    private final String iaExUiFrontendUrl;
    private final PersonalisationProvider personalisationProvider;
    private final CustomerServicesProvider customerServicesProvider;
    private final HomeOfficeEmailFinder hoEmailAddressFinder;



    public RespondentTurnOnNotificationsPersonalisation(
            @NotNull(message = "homeOfficeTurnOnNotificationsBeforeListingTemplateId cannot be null")
            @Value("${govnotify.template.turnOnNotifications.homeOffice.beforeListing.email}") String homeOfficeTurnOnNotificationsBeforeListingTemplateId,
            @NotNull(message = "homeOfficeTurnOnNotificationsAfterListingTemplateId cannot be null")
            @Value("${govnotify.template.turnOnNotifications.homeOffice.afterListing.email}") String homeOfficeTurnOnNotificationsAfterListingTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        PersonalisationProvider personalisationProvider,
        CustomerServicesProvider customerServicesProvider,
        HomeOfficeEmailFinder hoEmailAddressFinder
    ) {
        this.homeOfficeTurnOnNotificationsBeforeListingTemplateId = homeOfficeTurnOnNotificationsBeforeListingTemplateId;
        this.homeOfficeTurnOnNotificationsAfterListingTemplateId = homeOfficeTurnOnNotificationsAfterListingTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.personalisationProvider = personalisationProvider;
        this.customerServicesProvider = customerServicesProvider;
        this.hoEmailAddressFinder = hoEmailAddressFinder;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_TURN_ON_NOTIFICATIONS_RESPONDENT";
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return hoEmailAddressFinder.getRecipientsList(asylumCase);
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAppealListed(asylumCase)
                ? homeOfficeTurnOnNotificationsAfterListingTemplateId : homeOfficeTurnOnNotificationsBeforeListingTemplateId;
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("subjectPrefix", "Immigration and Asylum appeal")
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("upperTribunalReferenceNumber", callback.getCaseDetails().getCaseData().read(AsylumCaseDefinition.UPPER_TRIBUNAL_REFERENCE_NUMBER, String.class).orElse(""))
            .putAll(personalisationProvider.getRespondentHeaderPersonalisation(callback.getCaseDetails().getCaseData()));

        return listCaseFields.build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
                .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }
}
