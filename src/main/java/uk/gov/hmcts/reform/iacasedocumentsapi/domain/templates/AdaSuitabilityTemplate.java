package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AdaSuitabilityReviewDecision;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;

@Component
public class AdaSuitabilityTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;

    public AdaSuitabilityTemplate(
            @Value("${adaSuitabilityDocument.templateName}") String templateName
    ) {
        this.templateName = templateName;
    }

    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
            CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.put("hmcts", "[userImage:decisionsandreasons.png]");
        fieldValues.put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""));

        final String givenName = asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse("");
        final String familyName = asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse("");
        final String fullName = givenName.concat(" " + familyName);
        fieldValues.put("appellantFullName", fullName);

        final AdaSuitabilityReviewDecision adaSuitabilityReviewDecision =
                asylumCase.read(AsylumCaseDefinition.SUITABILITY_REVIEW_DECISION, AdaSuitabilityReviewDecision.class)
                        .orElseThrow(() -> new RequiredFieldMissingException("ADA suitability review decision unavailable."));
        fieldValues.put("suitability", adaSuitabilityReviewDecision);

        final String adaSuitabilityReason = asylumCase.read(SUITABILITY_REVIEW_REASON, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException("ADA suitability review decision reason unavailable."));
        fieldValues.put("suitabilityReason", adaSuitabilityReason);

        final String judgeName = asylumCase.read(SUITABILITY_REVIEW_JUDGE, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException("ADA suitability review decision judge details unavailable."));
        fieldValues.put("judgeName", judgeName);

        fieldValues.put("decisionDate", LocalDate.now().toString());

        return fieldValues;
    }

}
