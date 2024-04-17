package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;

@Service
public class BailCaseFileNameWithCaseIdQualifier implements FileNameQualifier<BailCase> {

    public String get(
        String unqualifiedFileName,
        CaseDetails<BailCase> caseDetails
    ) {
        final BailCase bailCase = caseDetails.getCaseData();

        final String applicantFamilyName =
            bailCase
                .read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException("Applicant Family Name is not present"));
        final String caseId = bailCase
            .read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class)
            .orElseThrow(() -> new RequiredFieldMissingException("Bail reference number is not present"));

        return caseId.trim() + "-" + applicantFamilyName.trim() + "-" + unqualifiedFileName;
    }
}
