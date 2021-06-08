package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.helper;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.END_APPEAL_APPROVER_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.END_APPEAL_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.END_APPEAL_OUTCOME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.END_APPEAL_OUTCOME_REASON;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;


@Component
public class EndAppealTemplateHelper {

    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");

    private final CustomerServicesProvider customerServicesProvider;

    public EndAppealTemplateHelper(CustomerServicesProvider customerServicesProvider) {
        this.customerServicesProvider = customerServicesProvider;
    }

    public Map<String, Object> getCommonMapFieldValues(
            CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.put("hmcts", "[userImage:hmcts.png]");

        fieldValues.put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""));
        fieldValues.put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""));
        fieldValues.put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""));

        fieldValues.put("outcomeOfAppeal", asylumCase.read(END_APPEAL_OUTCOME, String.class).orElse(""));
        fieldValues.put("reasonsOfOutcome", asylumCase.read(END_APPEAL_OUTCOME_REASON, String.class).orElse(""));
        fieldValues.put("endAppealDate", formatDateForRendering(asylumCase.read(END_APPEAL_DATE, String.class).orElse("")));
        fieldValues.put("endAppealApprover", asylumCase.read(END_APPEAL_APPROVER_NAME, String.class).orElse(""));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getCustomerServicesTelephone());
        fieldValues.put("customerServicesEmail", customerServicesProvider.getCustomerServicesEmail());

        return fieldValues;
    }

    private String formatDateForRendering(
            String date
    ) {
        if (!Strings.isNullOrEmpty(date)) {
            return LocalDate.parse(date).format(DOCUMENT_DATE_FORMAT);
        }

        return "";
    }

}
