package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.applicant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.applicant.ApplicantBailSmsNotificationPersonalisation;

@Service
public class ApplicantBailApplicationSubmittedPersonalisationSms implements ApplicantBailSmsNotificationPersonalisation {

    private final String applicationSubmittedApplicantSmsTemplateId;

    public ApplicantBailApplicationSubmittedPersonalisationSms(
            @Value("${govnotify.bail.template.submitApplication.sms}") String applicationSubmittedApplicantSmsTemplateId) {
        this.applicationSubmittedApplicantSmsTemplateId = applicationSubmittedApplicantSmsTemplateId;
    }

    @Override
    public String getTemplateId() {
        return applicationSubmittedApplicantSmsTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_APPLICATION_SUBMITTED_APPLICANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");

        return ImmutableMap
                .<String, String>builder()
                .put("bailReferenceNumber", bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse(""))
                .build();
    }
}
