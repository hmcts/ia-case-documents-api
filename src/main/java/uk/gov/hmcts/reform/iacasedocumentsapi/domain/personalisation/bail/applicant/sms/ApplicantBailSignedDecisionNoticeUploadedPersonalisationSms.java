package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.applicant.sms;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.BailCaseUtils.isBailGranted;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.applicant.ApplicantBailSmsNotificationPersonalisation;

@Service
public class ApplicantBailSignedDecisionNoticeUploadedPersonalisationSms implements ApplicantBailSmsNotificationPersonalisation {

    private final String signedDecisionNoticeUploadedApplicantSmsTemplateId;
    private static final String YES = "yes";
    private static final String NO = "no";

    public ApplicantBailSignedDecisionNoticeUploadedPersonalisationSms(
        @Value("${govnotify.bail.template.uploadSignedDecisionNotice.sms}") String signedDecisionNoticeUploadedApplicantSmsTemplateId) {
        this.signedDecisionNoticeUploadedApplicantSmsTemplateId = signedDecisionNoticeUploadedApplicantSmsTemplateId;
    }

    @Override
    public String getTemplateId() {
        return signedDecisionNoticeUploadedApplicantSmsTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_SIGNED_DECISION_NOTICE_UPLOADED_APPLICANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("bailReferenceNumber", bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("granted", Boolean.TRUE.equals(isBailGranted(bailCase)) ? YES : Boolean.FALSE.equals(isBailGranted(bailCase)) ? NO : "")
            .put("refused", Boolean.FALSE.equals(isBailGranted(bailCase)) ? YES : Boolean.TRUE.equals(isBailGranted(bailCase)) ? NO : "")
            .build();
    }
}
