package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "govnotify.template")
public class GovNotifyTemplateIdConfiguration {

    @NotBlank
    private String endAppealHomeOfficeTemplateId;

    @NotBlank
    private String endAppealLegalRepresentativeTemplateId;

    @NotBlank
    private String hearingBundleReadyLegalRepTemplateId;

    @NotBlank
    private String hearingBundleReadyHomeOfficeTemplateId;

    @NotBlank
    private String legalRepresentativeNonStandardDirectionTemplateId;

    @NotBlank
    private String legalRepNonStandardDirectionOfHomeOfficeTemplateId;

    @NotBlank
    private String caseOfficerRequestHearingRequirementsTemplateId;

    @NotBlank
    private String submittedHearingRequirementsLegalRepTemplateId;

    @NotBlank
    private String submittedHearingRequirementsCaseOfficerTemplateId;

    @NotBlank
    private String uploadedAdditionalEvidenceTemplateId;

    @NotBlank
    private String uploadedAddendumEvidenceTemplateId;

    @NotBlank
    private String changeDirectionDueDateTemplateId;

    @NotBlank
    private String changeDirectionDueDateOfHomeOfficeTemplateId;

    @NotBlank
    private String applicationGrantedApplicantLegalRep;

    @NotBlank
    private String applicationGrantedApplicantHomeOffice;

    @NotBlank
    private String applicationGrantedOtherPartyLegalRep;

    @NotBlank
    private String applicationGrantedOtherPartyHomeOffice;

    @NotBlank
    private String applicationGrantedAdmin;

    @NotBlank
    private String applicationPartiallyGrantedApplicantLegalRep;

    @NotBlank
    private String applicationPartiallyGrantedApplicantHomeOffice;

    @NotBlank
    private String applicationPartiallyGrantedOtherPartyLegalRep;

    @NotBlank
    private String applicationPartiallyGrantedOtherPartyHomeOffice;

    @NotBlank
    private String applicationPartiallyGrantedAdmin;

    @NotBlank
    private String applicationNotAdmittedApplicantLegalRep;

    @NotBlank
    private String applicationNotAdmittedApplicantHomeOffice;

    @NotBlank
    private String applicationNotAdmittedOtherPartyLegalRep;

    @NotBlank
    private String applicationNotAdmittedOtherPartyHomeOffice;

    @NotBlank
    private String applicationRefusedApplicantLegalRep;

    @NotBlank
    private String applicationRefusedApplicantHomeOffice;

    @NotBlank
    private String applicationRefusedOtherPartyLegalRep;

    @NotBlank
    private String applicationRefusedOtherPartyHomeOffice;

}
