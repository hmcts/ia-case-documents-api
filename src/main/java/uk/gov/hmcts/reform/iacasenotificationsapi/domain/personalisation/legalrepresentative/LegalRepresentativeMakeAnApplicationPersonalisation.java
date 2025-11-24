package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isInternalCase;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isNotInternalOrIsInternalWithLegalRepresentation;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;


@Service
public class LegalRepresentativeMakeAnApplicationPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private static final String ROLE_LEGAL_REP = "caseworker-ia-legalrep-solicitor";

    private final String legalRepresentativeMakeApplicationBeforeListingTemplateId;
    private final String legalRepresentativeMakeApplicationAfterListingTemplateId;
    private final String legalRepresentativeMakeApplicationOtherPartyBeforeListingTemplateId;
    private final String legalRepresentativeMakeApplicationOtherPartyAfterListingTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final AppealService appealService;
    private final MakeAnApplicationService makeAnApplicationService;
    private final UserDetailsProvider userDetailsProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public LegalRepresentativeMakeAnApplicationPersonalisation(
            @Value("${govnotify.template.makeAnApplication.beforeListing.legalRep.email}") String legalRepresentativeMakeApplicationBeforeListingTemplateId,
            @Value("${govnotify.template.makeAnApplication.afterListing.legalRep.email}") String legalRepresentativeMakeApplicationAfterListingTemplateId,
            @Value("${govnotify.template.makeAnApplication.beforeListing.otherParty.legalRep.email}") String legalRepresentativeMakeApplicationOtherPartyBeforeListingTemplateId,
            @Value("${govnotify.template.makeAnApplication.afterListing.otherParty.legalRep.email}") String legalRepresentativeMakeApplicationOtherPartyAfterListingTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            CustomerServicesProvider customerServicesProvider,
            AppealService appealService,
            UserDetailsProvider userDetailsProvider,
            MakeAnApplicationService makeAnApplicationService
    ) {
        requireNonNull(iaExUiFrontendUrl, "iaExUiFrontendUrl must not be null");
        this.legalRepresentativeMakeApplicationBeforeListingTemplateId = legalRepresentativeMakeApplicationBeforeListingTemplateId;
        this.legalRepresentativeMakeApplicationAfterListingTemplateId = legalRepresentativeMakeApplicationAfterListingTemplateId;
        this.legalRepresentativeMakeApplicationOtherPartyBeforeListingTemplateId = legalRepresentativeMakeApplicationOtherPartyBeforeListingTemplateId;
        this.legalRepresentativeMakeApplicationOtherPartyAfterListingTemplateId = legalRepresentativeMakeApplicationOtherPartyAfterListingTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.appealService = appealService;
        this.userDetailsProvider = userDetailsProvider;
        this.makeAnApplicationService = makeAnApplicationService;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return isInternalCase(asylumCase) && !isNotInternalOrIsInternalWithLegalRepresentation(asylumCase)
            ? Collections.emptySet()
            : LegalRepresentativeEmailNotificationPersonalisation.super.getRecipientsList(asylumCase);
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        boolean isAppealListed = appealService.isAppealListed(asylumCase);
        boolean isLegalRepUser = hasRole(ROLE_LEGAL_REP);

        if (isAppealListed) {
            return isLegalRepUser ?  legalRepresentativeMakeApplicationAfterListingTemplateId : legalRepresentativeMakeApplicationOtherPartyAfterListingTemplateId;
        } else {
            return isLegalRepUser ? legalRepresentativeMakeApplicationBeforeListingTemplateId : legalRepresentativeMakeApplicationOtherPartyBeforeListingTemplateId;
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_MAKE_AN_APPLICATION_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("applicationType", makeAnApplicationService.getMakeAnApplication(asylumCase, false).map(MakeAnApplication::getType).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
    }

    private boolean hasRole(String roleName) {
        return userDetailsProvider
                .getUserDetails()
                .getRoles()
                .contains(roleName);
    }
}
