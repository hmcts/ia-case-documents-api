package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.applyforcosts;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.RESPOND_TO_COSTS_LIST;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.HOME_OFFICE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getApplicantAndRespondent;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@Service
public class RespondToCostsRespondentPersonalisation implements EmailNotificationPersonalisation {
    private final String respondToCostsNotificationForRespondentTemplateId;
    private final String homeOfficeEmailAddress;
    private final EmailAddressFinder emailAddressFinder;
    private final PersonalisationProvider personalisationProvider;
    private final CustomerServicesProvider customerServicesProvider;

    public RespondToCostsRespondentPersonalisation(
        @Value("${govnotify.template.respondToCostsNotification.respondent.email}") String respondToCostsNotificationForRespondentTemplateId,
        @Value("${applyForCostsHomeOfficeEmailAddress}") String homeOfficeEmailAddress,
        EmailAddressFinder emailAddressFinder,
        CustomerServicesProvider customerServicesProvider,
        PersonalisationProvider personalisationProvider
    ) {
        this.respondToCostsNotificationForRespondentTemplateId = respondToCostsNotificationForRespondentTemplateId;
        this.homeOfficeEmailAddress = homeOfficeEmailAddress;
        this.emailAddressFinder = emailAddressFinder;
        this.customerServicesProvider = customerServicesProvider;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return respondToCostsNotificationForRespondentTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        ImmutablePair<String, String> applicantAndRespondent = getApplicantAndRespondent(asylumCase, getAppById -> AsylumCaseUtils.getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST));

        if (applicantAndRespondent.getRight().equals(HOME_OFFICE)) {
            return Collections.singleton(homeOfficeEmailAddress);
        } else {
            return Collections.singleton(emailAddressFinder.getLegalRepEmailAddress(asylumCase));
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RESPOND_TO_COSTS_RESPONDENT_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        ImmutablePair<String, String> applicantAndRespondent = getApplicantAndRespondent(asylumCase, getAppById -> AsylumCaseUtils.getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST));

        ImmutableMap.Builder<String, String> personalisationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(personalisationProvider.getApplyForCostsPersonalisation(asylumCase))
            .putAll(personalisationProvider.getTypeForSelectedApplyForCosts(asylumCase, RESPOND_TO_COSTS_LIST))
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .putAll(personalisationProvider.retrieveSelectedApplicationId(asylumCase, RESPOND_TO_COSTS_LIST));

        if (applicantAndRespondent.getRight().equals(HOME_OFFICE)) {
            personalisationBuilder.putAll(personalisationProvider.getHomeOfficeRecipientHeader(asylumCase));
        } else {
            personalisationBuilder.putAll(personalisationProvider.getLegalRepRecipientHeader(asylumCase));
        }

        return personalisationBuilder.build();
    }
}
