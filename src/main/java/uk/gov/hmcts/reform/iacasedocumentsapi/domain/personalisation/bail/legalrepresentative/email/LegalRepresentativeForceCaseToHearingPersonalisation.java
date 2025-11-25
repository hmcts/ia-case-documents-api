package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.*;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative.LegalRepresentativeBailEmailNotificationPersonalisation;

@Service
public class LegalRepresentativeForceCaseToHearingPersonalisation implements LegalRepresentativeBailEmailNotificationPersonalisation {

    private final String legalRepEmailTemplateId;

    public LegalRepresentativeForceCaseToHearingPersonalisation(
        @NotNull(message = "bailDocumentDeletedTemplateId cannot be null")
        @Value("${govnotify.bail.template.forceCaseToHearing.withLegalRep.legalRep}")
            String legalRepEmailTemplateId) {
        this.legalRepEmailTemplateId = legalRepEmailTemplateId;
    }

    @Override
    public String getTemplateId(BailCase bailCase) {
        return legalRepEmailTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_LEGAL_REP_FORCE_CASE_TO_HEARING";
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");
        return ImmutableMap.<String, String>builder()
            .put("bailReferenceNumber",
                bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReference",
                bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class).orElse(""))
            .put("applicantGivenNames", bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class).orElse(""))
            .put("applicantFamilyName", bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class).orElse(""))
            .put("homeOfficeReferenceNumber",
                bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .build();
    }
}
