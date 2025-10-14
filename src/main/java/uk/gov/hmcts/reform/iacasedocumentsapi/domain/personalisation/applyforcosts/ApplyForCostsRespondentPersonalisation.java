package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.applyforcosts;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.*;

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
public class ApplyForCostsRespondentPersonalisation implements EmailNotificationPersonalisation {

    private final String applyForCostsNotificationForRespondentTemplateId;
    private final String homeOfficeEmailAddress;
    private final EmailAddressFinder emailAddressFinder;
    private final PersonalisationProvider personalisationProvider;
    private final CustomerServicesProvider customerServicesProvider;

    public ApplyForCostsRespondentPersonalisation(
        @Value("${govnotify.template.applyForCostsNotification.respondent.email}") String applyForCostsNotificationForRespondentTemplateId,
        @Value("${applyForCostsHomeOfficeEmailAddress}") String homeOfficeEmailAddress,
        EmailAddressFinder emailAddressFinder,
        CustomerServicesProvider customerServicesProvider,
        PersonalisationProvider personalisationProvider
    ) {
        this.applyForCostsNotificationForRespondentTemplateId = applyForCostsNotificationForRespondentTemplateId;
        this.homeOfficeEmailAddress = homeOfficeEmailAddress;
        this.emailAddressFinder = emailAddressFinder;
        this.customerServicesProvider = customerServicesProvider;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return applyForCostsNotificationForRespondentTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        ImmutablePair<String, String> applicantAndRespondent = getApplicantAndRespondent(asylumCase, func -> retrieveLatestApplyForCosts(asylumCase));

        if (applicantAndRespondent.getRight().equals(LEGAL_REPRESENTATIVE)) {
            return Collections.singleton(emailAddressFinder.getLegalRepEmailAddress(asylumCase));
        } else {
            return Collections.singleton(homeOfficeEmailAddress);
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPLY_FOR_COSTS_RESPONDENT_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        ImmutablePair<String, String> applicantAndRespondent = getApplicantAndRespondent(asylumCase, func -> retrieveLatestApplyForCosts(asylumCase));

        ImmutableMap.Builder<String, String> personalisationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(personalisationProvider.getApplyToCostsCreationDate(asylumCase))
            .putAll(personalisationProvider.getTypeForLatestCreatedApplyForCosts(asylumCase))
            .putAll(personalisationProvider.getApplyForCostsPersonalisation(asylumCase))
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation());

        if (applicantAndRespondent.getRight().equals(LEGAL_REPRESENTATIVE)) {
            personalisationBuilder.putAll(personalisationProvider.getLegalRepRecipientHeader(asylumCase));
        } else {
            personalisationBuilder.putAll(personalisationProvider.getHomeOfficeRecipientHeader(asylumCase));
        }

        return personalisationBuilder.build();
    }
}
