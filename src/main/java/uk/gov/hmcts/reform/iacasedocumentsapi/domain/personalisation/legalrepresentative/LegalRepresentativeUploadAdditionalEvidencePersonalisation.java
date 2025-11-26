package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.JourneyType.AIP;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getLegalRepEmailInternalOrLegalRepJourney;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@Service
public class LegalRepresentativeUploadAdditionalEvidencePersonalisation implements EmailNotificationPersonalisation {

    private final String legalRepUploadedAdditionalEvidenceBeforeListingTemplateId;
    private final String legalRepUploadedAdditionalEvidenceAfterListingTemplateId;
    private final String iaExUiFrontendUrl;
    private final PersonalisationProvider personalisationProvider;
    private final CustomerServicesProvider customerServicesProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public LegalRepresentativeUploadAdditionalEvidencePersonalisation(
        @Value("${govnotify.template.uploadedAdditionalEvidenceBeforeListing.legalRep.email}") String legalRepUploadedAdditionalEvidenceBeforeListingTemplateId,
        @Value("${govnotify.template.uploadedAdditionalEvidenceAfterListing.legalRep.email}") String legalRepUploadedAdditionalEvidenceAfterListingTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        PersonalisationProvider personalisationProvider,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.legalRepUploadedAdditionalEvidenceBeforeListingTemplateId = legalRepUploadedAdditionalEvidenceBeforeListingTemplateId;
        this.legalRepUploadedAdditionalEvidenceAfterListingTemplateId = legalRepUploadedAdditionalEvidenceAfterListingTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.personalisationProvider = personalisationProvider;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAppealListed(asylumCase)
            ? legalRepUploadedAdditionalEvidenceAfterListingTemplateId : legalRepUploadedAdditionalEvidenceBeforeListingTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        if (isAipJourney(asylumCase) || asylumCase.read(CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class)
                .map(it -> it.getCaseRoleId() == null)
                .orElse(false)) {

            return Collections.emptySet();
        } else {
            return Collections.singleton(getLegalRepEmailInternalOrLegalRepJourney(asylumCase));
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_UPLOADED_ADDITIONAL_EVIDENCE_LEGAL_REP";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder();
        listCaseFields.putAll(customerServicesProvider.getCustomerServicesPersonalisation());
        listCaseFields.put("subjectPrefix", isAcceleratedDetainedAppeal(callback.getCaseDetails().getCaseData())
            ? adaPrefix
            : nonAdaPrefix);
        listCaseFields.put("linkToOnlineService", iaExUiFrontendUrl);
        listCaseFields.putAll(personalisationProvider.getPersonalisation(callback));

        return listCaseFields.build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
            .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }

    private boolean isAipJourney(AsylumCase asylumCase) {

        return asylumCase
                .read(JOURNEY_TYPE, JourneyType.class)
                .map(type -> type == AIP).orElse(false);
    }

}
