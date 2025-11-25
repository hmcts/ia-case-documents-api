package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@Service
public class LegalRepresentativeEditListingPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepresentativeCaseEditedNonAdaTemplateId;
    private final String legalRepresentativeCaseEditedAdaTemplateId;
    private final String legalRepresentativeCaseEditedRemoteHearingTemplateId;
    private final String listAssistHearingLegalRepresentativeCaseEditedTemplateId;
    private final String listAssistHearingLegalRepresentativeCaseEditedRemoteHearingTemplateId;
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
        @Value("${govnotify.template.listAssistHearing.caseEdited.legalRep.email}") String listAssistHearingLegalRepresentativeCaseEditedTemplateId,
        @Value("${govnotify.template.listAssistHearing.caseEditedRemoteHearing.legalRep.email}") String listAssistHearingLegalRepresentativeCaseEditedRemoteHearingTemplateId,
        PersonalisationProvider personalisationProvider,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.legalRepresentativeCaseEditedNonAdaTemplateId = legalRepresentativeCaseEditedNonAdaTemplateId;
        this.legalRepresentativeCaseEditedAdaTemplateId = legalRepresentativeCaseEditedAdaTemplateId;
        this.legalRepresentativeCaseEditedRemoteHearingTemplateId = legalRepresentativeCaseEditedRemoteHearingTemplateId;
        this.listAssistHearingLegalRepresentativeCaseEditedTemplateId = listAssistHearingLegalRepresentativeCaseEditedTemplateId;
        this.listAssistHearingLegalRepresentativeCaseEditedRemoteHearingTemplateId = listAssistHearingLegalRepresentativeCaseEditedRemoteHearingTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        YesOrNo isIntegrated = asylumCase.read(IS_INTEGRATED, YesOrNo.class).orElse(YesOrNo.NO);
        HearingCentre hearingCentre = asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class).orElse(null);
        if (AsylumCaseUtils.isAcceleratedDetainedAppeal(asylumCase)) {
            return legalRepresentativeCaseEditedAdaTemplateId;
        } else if (HearingCentre.REMOTE_HEARING == hearingCentre) {
            return isIntegrated == YesOrNo.YES ?
                listAssistHearingLegalRepresentativeCaseEditedRemoteHearingTemplateId : legalRepresentativeCaseEditedRemoteHearingTemplateId;
        } else {
            return isIntegrated == YesOrNo.YES ?
                listAssistHearingLegalRepresentativeCaseEditedTemplateId : legalRepresentativeCaseEditedNonAdaTemplateId;
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
