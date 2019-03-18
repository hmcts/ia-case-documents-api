package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;

@Service
public class AsylumCaseFileNameQualifier implements FileNameQualifier<AsylumCase> {

    public String get(
        String unqualifiedFileName,
        CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final String appealReferenceNumber =
            asylumCase
                .getAppealReferenceNumber()
                .orElseThrow(() -> new IllegalStateException("appealReferenceNumber is not present"));

        final String appellantFamilyName =
            asylumCase
                .getAppellantFamilyName()
                .orElseThrow(() -> new IllegalStateException("appellantFamilyName is not present"));

        return appealReferenceNumber.replace("/", " ")
               + "-" + appellantFamilyName
               + "-" + unqualifiedFileName;
    }
}
