package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;

@Service
public class BailCaseFileNameQualifier implements FileNameQualifier<BailCase> {

    public String get(
        String unqualifiedFileName,
        CaseDetails<BailCase> caseDetails
    ) {
        final BailCase bailCase = caseDetails.getCaseData();

        final String applicantFamilyName =
            bailCase
                .read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException("Applicant Family Name is not present"));

        return applicantFamilyName.trim()
            + "-" + unqualifiedFileName;
    }
}
