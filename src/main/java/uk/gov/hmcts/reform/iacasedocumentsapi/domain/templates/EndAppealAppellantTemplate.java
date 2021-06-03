package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.EmailAddressFinder;


@Component
public class EndAppealAppellantTemplate implements DocumentTemplate<AsylumCase> {

    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private final EmailAddressFinder emailAddressFinder;

    public EndAppealAppellantTemplate(
        @Value("${endAppeal.appellant.templateName}") String templateName,
        CustomerServicesProvider customerServicesProvider,
        EmailAddressFinder emailAddressFinder
    ) {
        this.templateName = templateName;
        this.customerServicesProvider = customerServicesProvider;
        this.emailAddressFinder = emailAddressFinder;
    }

    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
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
        fieldValues.put("designatedHearingCentre", emailAddressFinder.getHearingCentreEmailAddress(asylumCase));

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
