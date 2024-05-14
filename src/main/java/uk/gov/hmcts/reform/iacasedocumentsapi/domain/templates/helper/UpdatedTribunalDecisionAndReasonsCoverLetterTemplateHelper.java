package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;


@Component
public class UpdatedTribunalDecisionAndReasonsCoverLetterTemplateHelper {

    private final CustomerServicesProvider customerServicesProvider;

    public UpdatedTribunalDecisionAndReasonsCoverLetterTemplateHelper(CustomerServicesProvider customerServicesProvider) {
        this.customerServicesProvider = customerServicesProvider;
    }

    public Map<String, Object> getCommonMapFieldValues(
            CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.put("hmcts", "[userImage:hmcts.png]");
        fieldValues.put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""));
        fieldValues.put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""));
        fieldValues.put("allowed", asylumCase.read(UPDATED_APPEAL_DECISION, String.class)
                .orElseThrow(() -> new IllegalStateException("update appeal decision must be present"))
                .equals("Allowed") ? "Yes" : "No");
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getCustomerServicesTelephone());
        fieldValues.put("customerServicesEmail", customerServicesProvider.getCustomerServicesEmail());

        return fieldValues;
    }

}
