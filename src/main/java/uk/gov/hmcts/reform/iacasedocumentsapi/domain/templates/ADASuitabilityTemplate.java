package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AdaSuitabilityReviewDecision;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.TemplateUtils.getCaseData;
@Component
public class ADASuitabilityTemplate implements DocumentTemplate<AsylumCase> {
    private final String templateName;
    private final StringProvider stringProvider;

    public ADASuitabilityTemplate(
            @Value("${AdaSuitabilityDocument.templateName}") String templateName,
            StringProvider stringProvider
    ) {
        this.templateName = templateName;
        this.stringProvider = stringProvider;
    }

    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
            CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = getCaseData(caseDetails);

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.put("hmcts", "[userImage:hmcts.png]");
        fieldValues.put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""));

        final String givenName = asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse("");
        final String familyName = asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse("");
        final String fullName = givenName.concat(" " + familyName);

        fieldValues.put("appellantName", fullName);

        final AdaSuitabilityReviewDecision adaSuitabilityReviewDecision =
                asylumCase.read(AsylumCaseDefinition.SUITABILITY_REVIEW_DECISION, AdaSuitabilityReviewDecision.class)
                        .orElseThrow(() -> new RequiredFieldMissingException("ADA suitability review decision unavailable."));

        YesOrNo isSuitable = YesOrNo.NO;
        if (adaSuitabilityReviewDecision.equals(AdaSuitabilityReviewDecision.SUITABLE)) {
            isSuitable = YesOrNo.YES;
        }
        fieldValues.put("suitability", isSuitable);

        final String adaSuitabilityReason = asylumCase.read(SUITABILITY_REVIEW_REASON, String.class).orElse("");
        fieldValues.put("adaSuitabilityReason", adaSuitabilityReason);

        final String judgeName = asylumCase.read(SUITABILITY_REVIEW_JUDGE, String.class).orElse("");
        fieldValues.put("judgeName", judgeName);

        return fieldValues;
    }

}
