package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.caseofficer;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

@Service
public class CaseOfficerListCasePersonalisation implements EmailNotificationPersonalisation {

    private final String caseOfficerCaseListedTemplateId;
    private final String listAssistHearingCaseOfficerCaseListedTemplateId;
    private final String iaExUiFrontendUrl;
    private final DateTimeExtractor dateTimeExtractor;
    private final EmailAddressFinder emailAddressFinder;
    private final HearingDetailsFinder hearingDetailsFinder;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public CaseOfficerListCasePersonalisation(
            @Value("${govnotify.template.caseListed.caseOfficer.email}") String caseOfficerCaseListedTemplateId,
            @Value("${govnotify.template.listAssistHearing.caseListed.caseOfficer.email}") String listAssistHearingCaseOfficerCaseListedTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            DateTimeExtractor dateTimeExtractor,
            EmailAddressFinder emailAddressFinder,
            HearingDetailsFinder hearingDetailsFinder) {
        this.caseOfficerCaseListedTemplateId = caseOfficerCaseListedTemplateId;
        this.listAssistHearingCaseOfficerCaseListedTemplateId = listAssistHearingCaseOfficerCaseListedTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.dateTimeExtractor = dateTimeExtractor;
        this.emailAddressFinder = emailAddressFinder;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return asylumCase.read(IS_INTEGRATED, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES
                ? listAssistHearingCaseOfficerCaseListedTemplateId : caseOfficerCaseListedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getListCaseCaseOfficerHearingCentreEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CASE_LISTED_CASE_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .put("ariaListingReference", asylumCase.read(AsylumCaseDefinition.ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getHearingDateTime(asylumCase)))
                .put("hearingTime", dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getHearingDateTime(asylumCase)))
                .put("hearingCentreAddress", hearingDetailsFinder.getHearingCentreLocation(asylumCase))
                .put("remoteVideoCallTribunalResponse", asylumCase.read(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class).orElse(""))
                .build();
    }

}
