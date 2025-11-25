package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.applicant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.applicant.ApplicantBailSmsNotificationPersonalisation;

@Service
public class ApplicantBailApplicationEndedPersonalisationSms implements ApplicantBailSmsNotificationPersonalisation {

    private final String applicationEndedApplicantSmsTemplateId;

    public ApplicantBailApplicationEndedPersonalisationSms(
        @Value("${govnotify.bail.template.endApplication.sms}") String applicationEndedApplicantSmsTemplateId) {
        this.applicationEndedApplicantSmsTemplateId = applicationEndedApplicantSmsTemplateId;
    }

    @Override
    public String getTemplateId() {
        return applicationEndedApplicantSmsTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_APPLICATION_ENDED_APPLICANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("bailReferenceNumber", bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("endApplicationOutcome", bailCase.read(BailCaseFieldDefinition.END_APPLICATION_OUTCOME, String.class).orElse(""))
            .put("endApplicationDate", bailCase.read(BailCaseFieldDefinition.END_APPLICATION_DATE, String.class).orElse(""))
            .build();
    }
}
