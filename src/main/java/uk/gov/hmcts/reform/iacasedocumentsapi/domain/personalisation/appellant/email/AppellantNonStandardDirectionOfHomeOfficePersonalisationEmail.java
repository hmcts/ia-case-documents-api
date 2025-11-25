package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@Service
public class AppellantNonStandardDirectionOfHomeOfficePersonalisationEmail implements EmailNotificationPersonalisation {

    private final String iaAipFrontendUrl;
    private final PersonalisationProvider personalisationProvider;
    private final String appellantNonStandardDirectionBeforeListingTemplateId;
    private final String appellantNonStandardDirectionAfterListingTemplateId;
    private final String appellantNonStandardDirectionToAppellantAndRespondentBeforeListingTemplateId;
    private final String appellantNonStandardDirectionToAppellantAndRespondentAfterListingTemplateId;
    private final CustomerServicesProvider customerServicesProvider;
    private final RecipientsFinder recipientsFinder;
    private final DirectionFinder directionFinder;

    public AppellantNonStandardDirectionOfHomeOfficePersonalisationEmail(
            @Value("${govnotify.template.nonStandardDirectionOfHomeOfficeBeforeListing.appellant.email}") String appellantNonStandardDirectionBeforeListingTemplateId,
            @Value("${govnotify.template.nonStandardDirectionOfHomeOfficeAfterListing.appellant.email}") String appellantNonStandardDirectionAfterListingTemplateId,
            @Value("${govnotify.template.nonStandardDirectionToAppellantAndRespondentBeforeListing.appellant.email}") String appellantNonStandardDirectionToAppellantAndRespondentBeforeListingTemplateId,
            @Value("${govnotify.template.nonStandardDirectionToAppellantAndRespondentAfterListing.appellant.email}") String appellantNonStandardDirectionToAppellantAndRespondentAfterListingTemplateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            PersonalisationProvider personalisationProvider,
            CustomerServicesProvider customerServicesProvider,
            RecipientsFinder recipientsFinder,
            DirectionFinder directionFinder
    ) {
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.appellantNonStandardDirectionBeforeListingTemplateId = appellantNonStandardDirectionBeforeListingTemplateId;
        this.appellantNonStandardDirectionAfterListingTemplateId = appellantNonStandardDirectionAfterListingTemplateId;
        this.appellantNonStandardDirectionToAppellantAndRespondentBeforeListingTemplateId = appellantNonStandardDirectionToAppellantAndRespondentBeforeListingTemplateId;
        this.appellantNonStandardDirectionToAppellantAndRespondentAfterListingTemplateId = appellantNonStandardDirectionToAppellantAndRespondentAfterListingTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.customerServicesProvider = customerServicesProvider;
        this.recipientsFinder = recipientsFinder;
        this.directionFinder = directionFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        if (directionFinder
                .findFirst(asylumCase, DirectionTag.NONE)
                .map(direction -> direction.getParties().equals(Parties.APPELLANT_AND_RESPONDENT))
                .orElse(false)) {
            return isAppealListed(asylumCase)
                    ? appellantNonStandardDirectionToAppellantAndRespondentAfterListingTemplateId : appellantNonStandardDirectionToAppellantAndRespondentBeforeListingTemplateId;
        } else {
            return isAppealListed(asylumCase)
                    ? appellantNonStandardDirectionAfterListingTemplateId : appellantNonStandardDirectionBeforeListingTemplateId;
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPELLANT_NON_STANDARD_DIRECTION_OF_HOME_OFFICE_EMAIL";
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);

    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("linkToOnlineService", iaAipFrontendUrl)
                .putAll(personalisationProvider.getPersonalisation(callback));

        return listCaseFields.build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
                .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }
}
