package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.applyforcosts;

import static java.util.Objects.requireNonNull;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@Service
public class ApplyForCostsApplicantPersonalisation implements EmailNotificationPersonalisation {

    private final String applyForCostsNotificationForApplicantTemplateId;
    private final String homeOfficeEmailAddress;
    private final EmailAddressFinder emailAddressFinder;
    private final PersonalisationProvider personalisationProvider;
    private final CustomerServicesProvider customerServicesProvider;

    public ApplyForCostsApplicantPersonalisation(
        @Value("${govnotify.template.applyForCostsNotification.applicant.email}") String applyForCostsNotificationForApplicantTemplateId,
        @Value("${applyForCostsHomeOfficeEmailAddress}") String homeOfficeEmailAddress,
        EmailAddressFinder emailAddressFinder,
        CustomerServicesProvider customerServicesProvider,
        PersonalisationProvider personalisationProvider
    ) {
        this.applyForCostsNotificationForApplicantTemplateId = applyForCostsNotificationForApplicantTemplateId;
        this.homeOfficeEmailAddress = homeOfficeEmailAddress;
        this.emailAddressFinder = emailAddressFinder;
        this.customerServicesProvider = customerServicesProvider;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return applyForCostsNotificationForApplicantTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        ImmutablePair<String, String> applicantAndRespondent = getApplicantAndRespondent(asylumCase, func -> retrieveLatestApplyForCosts(asylumCase));
        if (applicantAndRespondent.getLeft().equals(HOME_OFFICE)) {
            return Collections.singleton(homeOfficeEmailAddress);
        } else {
            return Collections.singleton(emailAddressFinder.getLegalRepEmailAddress(asylumCase));
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPLY_FOR_COSTS_APPLICANT_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        ImmutablePair<String, String> applicantAndRespondent = getApplicantAndRespondent(asylumCase, func -> retrieveLatestApplyForCosts(asylumCase));

        ImmutableMap.Builder<String, String> personalisationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(personalisationProvider.getApplyForCostsPersonalisation(asylumCase))
            .putAll(personalisationProvider.getTypeForLatestCreatedApplyForCosts(asylumCase))
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .putAll(personalisationProvider.getApplyToCostsCreationDate(asylumCase));

        if (applicantAndRespondent.getLeft().equals(HOME_OFFICE)) {
            personalisationBuilder.putAll(personalisationProvider.getHomeOfficeRecipientHeader(asylumCase));
        } else {
            personalisationBuilder.putAll(personalisationProvider.getLegalRepRecipientHeader(asylumCase));
        }

        return personalisationBuilder.build();
    }
}
