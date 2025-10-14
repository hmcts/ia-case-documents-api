package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.applicant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.applicant.ApplicantBailSmsNotificationPersonalisation;

@Service
public class ApplicantBailNocChangedLrPersonalisationSms implements ApplicantBailSmsNotificationPersonalisation {

    private final String nocChangedLrApplicantSmsTemplateId;

    public ApplicantBailNocChangedLrPersonalisationSms(
            @Value("${govnotify.bail.template.nocChangedLRForApplicant.sms}") String nocChangedLrApplicantSmsTemplateId) {
        this.nocChangedLrApplicantSmsTemplateId = nocChangedLrApplicantSmsTemplateId;
    }

    @Override
    public String getTemplateId() {
        return nocChangedLrApplicantSmsTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_NOC_CHANGED_LR_APPLICANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");

        return ImmutableMap
                .<String, String>builder()
                .put("applicantGivenNames", bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class).orElse(""))
                .put("applicantFamilyName", bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class).orElse(""))
                .put("applicantDateOfBirth", bailCase.read(BailCaseFieldDefinition.APPLICANT_DATE_OF_BIRTH, String.class).orElse(""))
                .put("bailReferenceNumber", bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse(""))
                .build();
    }
}
