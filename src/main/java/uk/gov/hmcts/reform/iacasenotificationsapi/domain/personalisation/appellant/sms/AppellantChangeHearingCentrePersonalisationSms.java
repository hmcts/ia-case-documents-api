package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;


@Service
public class AppellantChangeHearingCentrePersonalisationSms implements SmsNotificationPersonalisation {

    private final String changeHearingCentreAppellantTemplateId;
    private final RecipientsFinder recipientsFinder;
    private final StringProvider stringProvider;


    public AppellantChangeHearingCentrePersonalisationSms(
            @Value("${govnotify.template.changeHearingCentre.appellant.sms}") String changeHearingCentreTemplateId,
            RecipientsFinder recipientsFinder,
            StringProvider stringProvider) {
        this.changeHearingCentreAppellantTemplateId = changeHearingCentreTemplateId;
        this.recipientsFinder = recipientsFinder;
        this.stringProvider = stringProvider;
    }

    @Override
    public String getTemplateId() {
        return changeHearingCentreAppellantTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CHANGE_HEARING_CENTRE_AIP_APPELLANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "asylumCase must not be null");
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        Optional<CaseDetails<AsylumCase>> caseDetailsBefore = callback.getCaseDetailsBefore();

        String oldHearingCentre = "";

        if (caseDetailsBefore.isPresent()) {
            oldHearingCentre = getHearingCentreName(caseDetailsBefore.get().getCaseData());
        }

        String newHearingCentre = getHearingCentreName(asylumCase);

        return ImmutableMap
            .<String, String>builder()
            .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("oldHearingCentre", oldHearingCentre)
            .put("newHearingCentre", newHearingCentre)
            .build();
    }

    private String getHearingCentreName(AsylumCase caseData) {

        String oldHearingCentre;
        HearingCentre mayBeOldHearingCentre = caseData.read(AsylumCaseDefinition.HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("hearingCentre is not present"));
        oldHearingCentre = stringProvider.get("hearingCentreName", mayBeOldHearingCentre.toString())
                .orElseThrow(() -> new IllegalStateException("hearingCentreName is not present: " + mayBeOldHearingCentre));
        return oldHearingCentre;
    }

}
