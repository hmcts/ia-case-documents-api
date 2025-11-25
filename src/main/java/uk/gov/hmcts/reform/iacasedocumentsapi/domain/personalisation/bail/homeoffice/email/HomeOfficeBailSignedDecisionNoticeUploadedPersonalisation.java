package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.homeoffice.email;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.BailEmailNotificationPersonalisation;

@Service
public class HomeOfficeBailSignedDecisionNoticeUploadedPersonalisation implements BailEmailNotificationPersonalisation {

    private final String homeOfficeBailSignedDecisionNoticeUploadedPersonalisationTemplateId;
    private final String homeOfficeBailSignedDecisionNoticeUploadedWithoutLrPersonalisationTemplateId;
    private final String bailHomeOfficeEmailAddress;


    public HomeOfficeBailSignedDecisionNoticeUploadedPersonalisation(
        @NotNull(message = "homeOfficeBailSignedDecisionNoticeUploadedPersonalisationTemplateId cannot be null")
        @Value("${govnotify.bail.template.uploadSignedDecisionNoticeHO.email}") String homeOfficeBailSignedDecisionNoticeUploadedPersonalisationTemplateId,
        @Value("${govnotify.bail.template.uploadSignedDecisionNoticeWithoutLR.email}") String homeOfficeBailSignedDecisionNoticeUploadedWithoutLrPersonalisationTemplateId,
        @Value("${bailHomeOfficeEmailAddress}") String bailHomeOfficeEmailAddress
    ) {
        this.homeOfficeBailSignedDecisionNoticeUploadedPersonalisationTemplateId = homeOfficeBailSignedDecisionNoticeUploadedPersonalisationTemplateId;
        this.homeOfficeBailSignedDecisionNoticeUploadedWithoutLrPersonalisationTemplateId = homeOfficeBailSignedDecisionNoticeUploadedWithoutLrPersonalisationTemplateId;
        this.bailHomeOfficeEmailAddress = bailHomeOfficeEmailAddress;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_UPLOADED_SIGNED_DECISION_NOTICE_HOME_OFFICE";
    }

    @Override
    public String getTemplateId(BailCase bailCase) {
        return isLegallyRepresented(bailCase)
            ? homeOfficeBailSignedDecisionNoticeUploadedPersonalisationTemplateId
            : homeOfficeBailSignedDecisionNoticeUploadedWithoutLrPersonalisationTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(BailCase bailCase) {
        return Collections.singleton(bailHomeOfficeEmailAddress);
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");

        if (isLegallyRepresented(bailCase)) {
            return ImmutableMap
                .<String, String>builder()
                .put("bailReferenceNumber", bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("legalRepReference", bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class).orElse(""))
                .put("applicantGivenNames", bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class).orElse(""))
                .put("applicantFamilyName", bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("decision", decision(bailCase))
                .build();
        }

        return ImmutableMap
            .<String, String>builder()
            .put("bailReferenceNumber", bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("applicantGivenNames", bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class).orElse(""))
            .put("applicantFamilyName", bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("decision", decision(bailCase))
            .build();
    }

}
