package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.applyforcosts;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.RESPOND_TO_COSTS_LIST;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@Service
public class RespondToCostsApplicantPersonalisation implements EmailNotificationPersonalisation {
    private final String respondToCostsNotificationForApplicantTemplateId;
    private final String homeOfficeEmailAddress;
    private final EmailAddressFinder emailAddressFinder;
    private final PersonalisationProvider personalisationProvider;
    private final CustomerServicesProvider customerServicesProvider;

    public RespondToCostsApplicantPersonalisation(
        @Value("${govnotify.template.respondToCostsNotification.applicant.email}") String respondToCostsNotificationForApplicantTemplateId,
        @Value("${applyForCostsHomeOfficeEmailAddress}") String homeOfficeEmailAddress,
        EmailAddressFinder emailAddressFinder,
        CustomerServicesProvider customerServicesProvider,
        PersonalisationProvider personalisationProvider
    ) {
        this.respondToCostsNotificationForApplicantTemplateId = respondToCostsNotificationForApplicantTemplateId;
        this.homeOfficeEmailAddress = homeOfficeEmailAddress;
        this.emailAddressFinder = emailAddressFinder;
        this.customerServicesProvider = customerServicesProvider;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return respondToCostsNotificationForApplicantTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        ImmutablePair<String, String> applicantAndRespondent = getApplicantAndRespondent(asylumCase, getAppById -> AsylumCaseUtils.getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST));
        if ((JUDGE).equals(applicantAndRespondent.getLeft())) {
            return Collections.emptySet();
        } else if (applicantAndRespondent.getLeft().equals(LEGAL_REPRESENTATIVE)) {
            return Collections.singleton(emailAddressFinder.getLegalRepEmailAddress(asylumCase));
        } else {
            return Collections.singleton(homeOfficeEmailAddress);
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RESPOND_TO_COSTS_APPLICANT_EMAIL";
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

        if (applicantAndRespondent.getLeft().equals(LEGAL_REPRESENTATIVE)) {
            personalisationBuilder.putAll(personalisationProvider.getLegalRepRecipientHeader(asylumCase));
        } else {
            personalisationBuilder.putAll(personalisationProvider.getHomeOfficeRecipientHeader(asylumCase));
        }

        return personalisationBuilder.build();
    }
}
