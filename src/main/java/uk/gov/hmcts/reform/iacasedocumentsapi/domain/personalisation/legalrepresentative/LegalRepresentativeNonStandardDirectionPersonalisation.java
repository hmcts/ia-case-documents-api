package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@Service
public class LegalRepresentativeNonStandardDirectionPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private static final String legalRepNonStandardDirectionSuffix = "_LEGAL_REP_NON_STANDARD_DIRECTION";
    private final String iaExUiFrontendUrl;
    private final PersonalisationProvider personalisationProvider;
    private final String legalRepresentativeNonStandardDirectionBeforeListingTemplateId;
    private final String legalRepresentativeNonStandardDirectionAfterListingTemplateId;
    private final CustomerServicesProvider customerServicesProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;


    public LegalRepresentativeNonStandardDirectionPersonalisation(
        @Value("${govnotify.template.nonStandardDirectionBeforeListing.legalRep.email}") String legalRepresentativeNonStandardDirectionBeforeListingTemplateId,
        @Value("${govnotify.template.nonStandardDirectionAfterListing.legalRep.email}") String legalRepresentativeNonStandardDirectionAfterListingTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        PersonalisationProvider personalisationProvider,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.legalRepresentativeNonStandardDirectionBeforeListingTemplateId = legalRepresentativeNonStandardDirectionBeforeListingTemplateId;
        this.legalRepresentativeNonStandardDirectionAfterListingTemplateId = legalRepresentativeNonStandardDirectionAfterListingTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAppealListed(asylumCase)
            ? legalRepresentativeNonStandardDirectionAfterListingTemplateId : legalRepresentativeNonStandardDirectionBeforeListingTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + legalRepNonStandardDirectionSuffix;
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("subjectPrefix", isAcceleratedDetainedAppeal(callback.getCaseDetails().getCaseData()) ? adaPrefix : nonAdaPrefix)
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .putAll(personalisationProvider.getPersonalisation(callback));

        return listCaseFields.build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
            .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }
}
