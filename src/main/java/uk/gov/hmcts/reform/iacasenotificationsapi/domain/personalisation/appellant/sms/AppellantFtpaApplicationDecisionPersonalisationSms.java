package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPLICANT_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ApplicantType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.FtpaNotificationPersonalisationUtil;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@Service
public class AppellantFtpaApplicationDecisionPersonalisationSms implements SmsNotificationPersonalisation, FtpaNotificationPersonalisationUtil {

    private final String ftpaRespondentDecisionGrantedPartiallyGrantedToAppellantSmsTemplateId;
    private final String ftpaRespondentDecisionNotAdmittedToAppellantSmsTemplateId;
    private final String ftpaRespondentDecisionRefusedToAppellantSmsTemplateId;
    private final String ftpaAppellantDecisionGrantedToAppellantSmsTemplateId;
    private final String ftpaAppellantDecisionPartiallyGrantedToAppellantSmsTemplateId;
    private final String ftpaAppellantDecisionNotAdmittedToAppellantSmsTemplateId;
    private final String ftpaAppellantDecisionRefusedToAppellantSmsTemplateId;
    private final String iaAipFrontendUrl;
    private final long oocDays;
    private final long inCountryDays;
    private final RecipientsFinder recipientsFinder;

    public AppellantFtpaApplicationDecisionPersonalisationSms(
        @Value("${govnotify.template.applicationGranted.otherParty.citizen.sms}") String ftpaRespondentDecisionGrantedPartiallyGrantedToAppellantSmsTemplateId,
        @Value("${govnotify.template.applicationNotAdmitted.otherParty.citizen.sms}") String ftpaRespondentDecisionNotAdmittedToAppellantSmsTemplateId,
        @Value("${govnotify.template.applicationRefused.otherParty.citizen.sms}") String ftpaRespondentDecisionRefusedToAppellantSmsTemplateId,
        @Value("${govnotify.template.applicationGranted.applicant.citizen.sms}") String ftpaAppellantDecisionGrantedToAppellantSmsTemplateId,
        @Value("${govnotify.template.applicationPartiallyGranted.applicant.citizen.sms}") String ftpaAppellantDecisionPartiallyGrantedToAppellantSmsTemplateId,
        @Value("${govnotify.template.applicationNotAdmitted.applicant.citizen.sms}") String ftpaAppellantDecisionNotAdmittedToAppellantSmsTemplateId,
        @Value("${govnotify.template.applicationRefused.applicant.citizen.sms}") String ftpaAppellantDecisionRefusedToAppellantSmsTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        @Value("${ftpaOutOfCountryDays}") long oocDays,
        @Value("${ftpaInCountryDays}") long inCountryDays,
        RecipientsFinder recipientsFinder
    ) {
        this.ftpaRespondentDecisionGrantedPartiallyGrantedToAppellantSmsTemplateId = ftpaRespondentDecisionGrantedPartiallyGrantedToAppellantSmsTemplateId;
        this.ftpaRespondentDecisionNotAdmittedToAppellantSmsTemplateId = ftpaRespondentDecisionNotAdmittedToAppellantSmsTemplateId;
        this.ftpaRespondentDecisionRefusedToAppellantSmsTemplateId = ftpaRespondentDecisionRefusedToAppellantSmsTemplateId;
        this.ftpaAppellantDecisionGrantedToAppellantSmsTemplateId = ftpaAppellantDecisionGrantedToAppellantSmsTemplateId;
        this.ftpaAppellantDecisionPartiallyGrantedToAppellantSmsTemplateId = ftpaAppellantDecisionPartiallyGrantedToAppellantSmsTemplateId;
        this.ftpaAppellantDecisionNotAdmittedToAppellantSmsTemplateId = ftpaAppellantDecisionNotAdmittedToAppellantSmsTemplateId;
        this.ftpaAppellantDecisionRefusedToAppellantSmsTemplateId = ftpaAppellantDecisionRefusedToAppellantSmsTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.oocDays = oocDays;
        this.inCountryDays = inCountryDays;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        String applicantType = asylumCase
            .read(FTPA_APPLICANT_TYPE, ApplicantType.class)
            .map(ApplicantType::getValue)
            .orElseThrow(() -> new IllegalStateException("ftpaApplicantType is not present"));

        switch (getDecisionOutcomeType(asylumCase)) {
            case FTPA_GRANTED:
                return applicantType.equals(APPELLANT_APPLICANT)
                    ? ftpaAppellantDecisionGrantedToAppellantSmsTemplateId
                    : ftpaRespondentDecisionGrantedPartiallyGrantedToAppellantSmsTemplateId;
            case FTPA_PARTIALLY_GRANTED:
                return applicantType.equals(APPELLANT_APPLICANT)
                    ? ftpaAppellantDecisionPartiallyGrantedToAppellantSmsTemplateId
                    : ftpaRespondentDecisionGrantedPartiallyGrantedToAppellantSmsTemplateId;
            case FTPA_REFUSED:
                return applicantType.equals(APPELLANT_APPLICANT)
                    ? ftpaAppellantDecisionRefusedToAppellantSmsTemplateId
                    : ftpaRespondentDecisionRefusedToAppellantSmsTemplateId;
            default:
                return applicantType.equals(APPELLANT_APPLICANT)
                    ? ftpaAppellantDecisionNotAdmittedToAppellantSmsTemplateId
                    : ftpaRespondentDecisionNotAdmittedToAppellantSmsTemplateId;
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_FTPA_APPLICATION_DECISION_TO_APPELLANT_SMS";
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("applicationDecision", ftpaDecisionVerbalization(getDecisionOutcomeType(asylumCase)))
            .put("linkToService", iaAipFrontendUrl)
            .put("dueDate", asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)
                .map(inUk -> inUk.equals(YES) ? dueDate(inCountryDays) : dueDate(oocDays)).orElse(""))
            .build();
    }
}
