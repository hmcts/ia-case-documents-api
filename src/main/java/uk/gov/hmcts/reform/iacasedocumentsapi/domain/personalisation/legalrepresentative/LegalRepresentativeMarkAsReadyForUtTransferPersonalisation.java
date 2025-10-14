package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAppealListed;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@Service
public class LegalRepresentativeMarkAsReadyForUtTransferPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String markReadyForUtTransferBeforeListingTemplateId;
    private final String markReadyForUtTransferAfterListingTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final PersonalisationProvider personalisationProvider;
    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public LegalRepresentativeMarkAsReadyForUtTransferPersonalisation(
        @NotNull(message = "markReadyForUtTransferBeforeListingTemplateId cannot be null")
        @Value("${govnotify.template.markAsReadyForUtTransfer.legalRep.beforeListing.email}") String markReadyForUtTransferBeforeListingTemplateId,
        @NotNull(message = "markReadyForUtTransferAfterListingTemplateId cannot be null")
        @Value("${govnotify.template.markAsReadyForUtTransfer.legalRep.afterListing.email}") String markReadyForUtTransferAfterListingTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        PersonalisationProvider personalisationProvider,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.markReadyForUtTransferBeforeListingTemplateId = markReadyForUtTransferBeforeListingTemplateId;
        this.markReadyForUtTransferAfterListingTemplateId = markReadyForUtTransferAfterListingTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.personalisationProvider = personalisationProvider;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAppealListed(asylumCase)
            ? markReadyForUtTransferAfterListingTemplateId : markReadyForUtTransferBeforeListingTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_MARK_AS_READY_FOR_UT_TRANSFER_LEGAL_REP";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .putAll(personalisationProvider.getLegalRepHeaderPersonalisation(asylumCase))
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("utAppealReferenceNumber", asylumCase.read(AsylumCaseDefinition.UT_APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .build();
    }
}
