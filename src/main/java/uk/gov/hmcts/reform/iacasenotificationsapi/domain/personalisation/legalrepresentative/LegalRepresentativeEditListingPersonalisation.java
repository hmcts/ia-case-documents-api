package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class LegalRepresentativeEditListingPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepresentativeCaseEditedNonAdaTemplateId;
    private final String legalRepresentativeCaseEditedAdaTemplateId;
    private final String legalRepresentativeCaseEditedRemoteHearingTemplateId;
    private final PersonalisationProvider personalisationProvider;
    private final CustomerServicesProvider customerServicesProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public LegalRepresentativeEditListingPersonalisation(
        @Value("${govnotify.template.caseEdited.legalRep.email.nonAda}") String legalRepresentativeCaseEditedNonAdaTemplateId,
        @Value("${govnotify.template.caseEdited.legalRep.email.ada}") String legalRepresentativeCaseEditedAdaTemplateId,
        @Value("${govnotify.template.caseEditedRemoteHearing.legalRep.email}") String legalRepresentativeCaseEditedRemoteHearingTemplateId,
        PersonalisationProvider personalisationProvider,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.legalRepresentativeCaseEditedNonAdaTemplateId = legalRepresentativeCaseEditedNonAdaTemplateId;
        this.legalRepresentativeCaseEditedAdaTemplateId = legalRepresentativeCaseEditedAdaTemplateId;
        this.legalRepresentativeCaseEditedRemoteHearingTemplateId = legalRepresentativeCaseEditedRemoteHearingTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        HearingCentre hearingCentre = asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class).orElse(null);
        if (AsylumCaseUtils.isAcceleratedDetainedAppeal(asylumCase)) {
            return legalRepresentativeCaseEditedAdaTemplateId;
        } else if (HearingCentre.REMOTE_HEARING == hearingCentre) {
            return legalRepresentativeCaseEditedRemoteHearingTemplateId;
        } else {
            return legalRepresentativeCaseEditedNonAdaTemplateId;
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CASE_RE_LISTED_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .putAll(personalisationProvider.getPersonalisation(callback))
            .put("subjectPrefix", isAcceleratedDetainedAppeal(callback.getCaseDetails().getCaseData())
                ? adaPrefix
                : nonAdaPrefix);

        return listCaseFields.build();
    }
}
