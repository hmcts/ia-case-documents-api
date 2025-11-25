package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.MakeAnApplicationService;

@Service
public class LegalRepresentativeDecideAnApplicationPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private static final String ROLE_LEGAL_REP = "caseworker-ia-legalrep-solicitor";

    private final String legalRepresentativeDecideAnApplicationGrantedBeforeListingTemplateId;
    private final String legalRepresentativeDecideAnApplicationGrantedAfterListingTemplateId;
    private final String legalRepresentativeDecideAnApplicationGrantedOtherPartyBeforeListingTemplateId;
    private final String legalRepresentativeDecideAnApplicationGrantedOtherPartyAfterListingTemplateId;

    private final String legalRepresentativeDecideAnApplicationRefusedBeforeListingTemplateId;
    private final String legalRepresentativeDecideAnApplicationRefusedAfterListingTemplateId;
    private final String legalRepresentativeDecideAnApplicationRefusedOtherPartyBeforeListingTemplateId;
    private final String legalRepresentativeDecideAnApplicationRefusedOtherPartyAfterListingTemplateId;

    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final MakeAnApplicationService makeAnApplicationService;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public LegalRepresentativeDecideAnApplicationPersonalisation(
            @Value("${govnotify.template.decideAnApplication.granted.applicant.legalRep.beforeListing.email}") String legalRepDecideAnApplicationGrantedBeforeListingTemplateId,
            @Value("${govnotify.template.decideAnApplication.granted.applicant.legalRep.afterListing.email}") String legalRepDecideAnApplicationGrantedAfterListingTemplateId,
            @Value("${govnotify.template.decideAnApplication.granted.otherParty.legalRep.beforeListing.email}") String legalRepDecideAnApplicationGrantedOtherPartyBeforeListingTemplateId,
            @Value("${govnotify.template.decideAnApplication.granted.otherParty.legalRep.afterListing.email}") String legalRepDecideAnApplicationGrantedOtherPartyAfterListingTemplateId,

            @Value("${govnotify.template.decideAnApplication.refused.applicant.legalRep.beforeListing.email}") String legalRepDecideAnApplicationRefusedBeforeListingTemplateId,
            @Value("${govnotify.template.decideAnApplication.refused.applicant.legalRep.afterListing.email}") String legalRepDecideAnApplicationRefusedAfterListingTemplateId,
            @Value("${govnotify.template.decideAnApplication.refused.otherParty.legalRep.beforeListing.email}") String legalRepDecideAnApplicationRefusedOtherPartyBeforeListingTemplateId,
            @Value("${govnotify.template.decideAnApplication.refused.otherParty.legalRep.afterListing.email}") String legalRepDecideAnApplicationRefusedOtherPartyAfterListingTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            CustomerServicesProvider customerServicesProvider,
            MakeAnApplicationService makeAnApplicationService
    ) {
        requireNonNull(iaExUiFrontendUrl, "iaExUiFrontendUrl must not be null");
        this.legalRepresentativeDecideAnApplicationGrantedBeforeListingTemplateId = legalRepDecideAnApplicationGrantedBeforeListingTemplateId;
        this.legalRepresentativeDecideAnApplicationGrantedAfterListingTemplateId = legalRepDecideAnApplicationGrantedAfterListingTemplateId;
        this.legalRepresentativeDecideAnApplicationGrantedOtherPartyBeforeListingTemplateId = legalRepDecideAnApplicationGrantedOtherPartyBeforeListingTemplateId;
        this.legalRepresentativeDecideAnApplicationGrantedOtherPartyAfterListingTemplateId = legalRepDecideAnApplicationGrantedOtherPartyAfterListingTemplateId;
        this.legalRepresentativeDecideAnApplicationRefusedBeforeListingTemplateId = legalRepDecideAnApplicationRefusedBeforeListingTemplateId;
        this.legalRepresentativeDecideAnApplicationRefusedAfterListingTemplateId = legalRepDecideAnApplicationRefusedAfterListingTemplateId;
        this.legalRepresentativeDecideAnApplicationRefusedOtherPartyBeforeListingTemplateId = legalRepDecideAnApplicationRefusedOtherPartyBeforeListingTemplateId;
        this.legalRepresentativeDecideAnApplicationRefusedOtherPartyAfterListingTemplateId = legalRepDecideAnApplicationRefusedOtherPartyAfterListingTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.makeAnApplicationService = makeAnApplicationService;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        Optional<MakeAnApplication> maybeMakeAnApplication = getMakeAnApplication(asylumCase);

        if (maybeMakeAnApplication.isPresent()) {
            MakeAnApplication makeAnApplication = maybeMakeAnApplication.get();
            String decision = makeAnApplication.getDecision();
            String applicantRole = makeAnApplication.getApplicantRole();

            String listingRef = asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(null);
            boolean isLegalRepUser = applicantRole.equals(ROLE_LEGAL_REP);

            if (listingRef != null) {
                if ("Granted".equals(decision)) {
                    return isLegalRepUser ?  legalRepresentativeDecideAnApplicationGrantedAfterListingTemplateId : legalRepresentativeDecideAnApplicationGrantedOtherPartyAfterListingTemplateId;
                } else {
                    return isLegalRepUser ?  legalRepresentativeDecideAnApplicationRefusedAfterListingTemplateId : legalRepresentativeDecideAnApplicationRefusedOtherPartyAfterListingTemplateId;
                }
            } else {
                if ("Granted".equals(decision)) {
                    return isLegalRepUser ?  legalRepresentativeDecideAnApplicationGrantedBeforeListingTemplateId : legalRepresentativeDecideAnApplicationGrantedOtherPartyBeforeListingTemplateId;
                } else {
                    return isLegalRepUser ?  legalRepresentativeDecideAnApplicationRefusedBeforeListingTemplateId : legalRepresentativeDecideAnApplicationRefusedOtherPartyBeforeListingTemplateId;
                }
            }
        } else {
            return "";
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_DECIDE_AN_APPLICATION_LEGAL_REPRESENTATIVE";
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
                .put("applicationType", getMakeAnApplication(asylumCase).map(it -> it.getType()).orElse(""))
                .put("applicationDecisionReason", getMakeAnApplication(asylumCase).map(it -> it.getDecisionReason()).orElse("No reason given"))
                .put("decisionMaker", getMakeAnApplication(asylumCase).map(it -> it.getDecisionMaker()).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
    }

    private Optional<MakeAnApplication> getMakeAnApplication(AsylumCase asylumCase) {
        return makeAnApplicationService.getMakeAnApplication(asylumCase, true);
    }
}
