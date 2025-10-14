package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.applyforcosts;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ADD_EVIDENCE_FOR_COSTS_LIST;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.*;

@Service
public class AddEvidenceForCostsSubmittedSubmitterPersonalisation implements EmailNotificationPersonalisation {
    private final String addEvidenceForCostsSubmitterPersonalisation;
    private final String homeOfficeEmailAddress;
    private final EmailAddressFinder emailAddressFinder;
    private final PersonalisationProvider personalisationProvider;
    private final CustomerServicesProvider customerServicesProvider;

    public AddEvidenceForCostsSubmittedSubmitterPersonalisation(
        @Value("${govnotify.template.addEvidenceForCosts.submitter.email}") String addEvidenceForCostsSubmitterPersonalisation,
        @Value("${applyForCostsHomeOfficeEmailAddress}") String homeOfficeEmailAddress,
        EmailAddressFinder emailAddressFinder,
        CustomerServicesProvider customerServicesProvider,
        PersonalisationProvider personalisationProvider
    ) {
        this.addEvidenceForCostsSubmitterPersonalisation = addEvidenceForCostsSubmitterPersonalisation;
        this.homeOfficeEmailAddress = homeOfficeEmailAddress;
        this.emailAddressFinder = emailAddressFinder;
        this.customerServicesProvider = customerServicesProvider;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return addEvidenceForCostsSubmitterPersonalisation;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {

        ImmutablePair<String, String> applicantAndRespondent = getApplicantAndRespondent(asylumCase, getAppById -> AsylumCaseUtils.getApplicationById(asylumCase, ADD_EVIDENCE_FOR_COSTS_LIST));

        if ((JUDGE).equals(applicantAndRespondent.getLeft())) {
            return Collections.emptySet();
        }

        if (toLegalRep(asylumCase)) {
            return Collections.singleton(emailAddressFinder.getLegalRepEmailAddress(asylumCase));
        } else {
            return Collections.singleton(homeOfficeEmailAddress);
        }
    }

    private static boolean toLegalRep(AsylumCase asylumCase) {
        return !isLoggedUserIsHomeOffice(asylumCase,
            getAppById -> AsylumCaseUtils.getApplicationById(asylumCase, ADD_EVIDENCE_FOR_COSTS_LIST));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_ADD_EVIDENCE_FOR_COSTS_SUBMITTER_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        String listingReferenceLine = "";
        String legalRepReferenceLine = "";
        String hoReferenceLine = "";

        if (toLegalRep(asylumCase)) {
            listingReferenceLine = asylumCase.read(ARIA_LISTING_REFERENCE, String.class)
                .map(ref -> "\nListing reference: " + ref)
                .orElse("");

            legalRepReferenceLine = asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)
                .map(ref -> "\nYour reference: " + ref)
                .orElse("");
        } else {
            hoReferenceLine = asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)
                .map(ref -> "\nHome Office reference: " + ref)
                .orElse("");
        }

        ImmutableMap.Builder<String, String> personalisationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .putAll(personalisationProvider.getApplyForCostsPersonalisation(asylumCase))
            .putAll(personalisationProvider.getTypeForSelectedApplyForCosts(asylumCase, ADD_EVIDENCE_FOR_COSTS_LIST))
            .putAll(personalisationProvider.retrieveSelectedApplicationId(asylumCase, ADD_EVIDENCE_FOR_COSTS_LIST))
            .put("ariaListingReference", listingReferenceLine)
            .put("legalRepReferenceNumber", legalRepReferenceLine)
            .put("homeOfficeReferenceNumber", hoReferenceLine);

        return personalisationBuilder.build();
    }
}
