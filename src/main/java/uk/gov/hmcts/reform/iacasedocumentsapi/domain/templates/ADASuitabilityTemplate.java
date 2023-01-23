package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

import java.time.format.DateTimeFormatter;
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

        String givenName = asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse("");
        String familyName = asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse("");
        String fullName = givenName.concat(" " + familyName);
        fieldValues.put("appellantName", fullName);
//        fieldValues.put("appellantTitle", asylumCase.read(APPELLANT_TITLE, String.class).orElse(""));

//        asylumCase.read(AsylumCaseFieldDefinition.SUITABILITY_REVIEW_DECISION, AdaSuitabilityReviewDecision.class)

//        fieldValues.put("adaSuitable", adaSuitability);
//        String adaSuitabilityReason = asylumCase.read(ADA_SUITABILITY_REASON, String.class).orElse("");
//        fieldValues.put("adaSuitabilityReason", adaSuitabilityReason);

//        String judgeName = asylumCase.read(JUDGE_NAME, String.class).or("");

        return fieldValues;
    }

}
