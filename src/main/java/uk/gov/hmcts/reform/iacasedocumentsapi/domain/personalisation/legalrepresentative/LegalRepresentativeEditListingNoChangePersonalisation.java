package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@Service
public class LegalRepresentativeEditListingNoChangePersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepresentativeCaseEditedNoChangeTemplateId;
    private final String legalRepresentativeCaseEditedRemoteHearingTemplateId;
    private final PersonalisationProvider personalisationProvider;
    private final CustomerServicesProvider customerServicesProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public LegalRepresentativeEditListingNoChangePersonalisation(
        @Value("${govnotify.template.caseEditedNoChange.legalRep.email}") String legalRepresentativeCaseEditedNoChangeTemplateId,
        @Value("${govnotify.template.caseEditedRemoteHearing.legalRep.email}") String legalRepresentativeCaseEditedRemoteHearingTemplateId,
        PersonalisationProvider personalisationProvider,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.legalRepresentativeCaseEditedNoChangeTemplateId = legalRepresentativeCaseEditedNoChangeTemplateId;
        this.legalRepresentativeCaseEditedRemoteHearingTemplateId = legalRepresentativeCaseEditedRemoteHearingTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        return asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class).equals(Optional.of(HearingCentre.REMOTE_HEARING))
            ? legalRepresentativeCaseEditedRemoteHearingTemplateId : legalRepresentativeCaseEditedNoChangeTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CASE_RE_LISTED_NO_CHANGE_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .putAll(personalisationProvider.getPersonalisation(callback))
            .put("subjectPrefix", isAcceleratedDetainedAppeal(callback.getCaseDetails().getCaseData()) ? adaPrefix : nonAdaPrefix);

        return listCaseFields.build();
    }
}
