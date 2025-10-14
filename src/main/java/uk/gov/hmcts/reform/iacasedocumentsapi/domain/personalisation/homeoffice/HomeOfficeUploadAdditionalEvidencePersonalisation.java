package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@Service
public class HomeOfficeUploadAdditionalEvidencePersonalisation implements EmailNotificationPersonalisation {

    private final String homeOfficeUploadedAdditionalEvidenceBeforeListingTemplateId;
    private final String homeOfficeUploadedAdditionalEvidenceAfterListingTemplateId;
    private final String iaExUiFrontendUrl;
    private final PersonalisationProvider personalisationProvider;
    private final String homeOfficeEmailAddress;
    private final CustomerServicesProvider customerServicesProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public HomeOfficeUploadAdditionalEvidencePersonalisation(
        @Value("${govnotify.template.uploadedAdditionalEvidenceBeforeListing.homeOffice.email}") String homeOfficeUploadedAdditionalEvidenceBeforeListingTemplateId,
        @Value("${govnotify.template.uploadedAdditionalEvidenceAfterListing.homeOffice.email}") String homeOfficeUploadedAdditionalEvidenceAfterListingTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        PersonalisationProvider personalisationProvider,
        @Value("${respondentEmailAddresses.respondentReviewDirection}") String homeOfficeEmailAddress,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.homeOfficeUploadedAdditionalEvidenceBeforeListingTemplateId = homeOfficeUploadedAdditionalEvidenceBeforeListingTemplateId;
        this.homeOfficeUploadedAdditionalEvidenceAfterListingTemplateId = homeOfficeUploadedAdditionalEvidenceAfterListingTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.personalisationProvider = personalisationProvider;
        this.homeOfficeEmailAddress = homeOfficeEmailAddress;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAppealListed(asylumCase)
            ? homeOfficeUploadedAdditionalEvidenceAfterListingTemplateId : homeOfficeUploadedAdditionalEvidenceBeforeListingTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(homeOfficeEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_UPLOADED_ADDITIONAL_EVIDENCE_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("subjectPrefix", isAcceleratedDetainedAppeal(callback.getCaseDetails().getCaseData())
                ? adaPrefix
                : nonAdaPrefix)
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


