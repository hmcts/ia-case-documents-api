package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.applicant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.applicant.ApplicantBailSmsNotificationPersonalisation;

@Service
public class ApplicantBailStopLegalRepresentingPersonalisationSms implements ApplicantBailSmsNotificationPersonalisation {

    private final String stopLegalRepresentingApplicantSmsTemplateId;
    private static final DateTimeFormatter NOTIFICATION_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy");

    public ApplicantBailStopLegalRepresentingPersonalisationSms(
            @Value("${govnotify.bail.template.stopLegalRepresenting.sms}") String stopLegalRepresentingApplicantSmsTemplateId) {
        this.stopLegalRepresentingApplicantSmsTemplateId = stopLegalRepresentingApplicantSmsTemplateId;
    }

    @Override
    public String getTemplateId() {
        return stopLegalRepresentingApplicantSmsTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_STOP_LEGAL_REPRESENTING_APPLICANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");

        return ImmutableMap
                .<String, String>builder()
                .put("bailReferenceNumber", bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("legalRepReference", bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class).orElse(""))
                .put("applicantGivenNames", bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class).orElse(""))
                .put("applicantFamilyName", bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class).orElse(""))
                .put("applicantDateOfBirth", LocalDate.parse(bailCase.read(BailCaseFieldDefinition.APPLICANT_DATE_OF_BIRTH, String.class)
                        .orElse("")).format(NOTIFICATION_DATE_FORMAT))
                .build();
    }
}
