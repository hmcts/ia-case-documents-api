package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

@Component
public class InternalRecordOutOfTimeDecisionLetterTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy");
    private final SystemDateProvider systemDateProvider;
    private final int daysAfterSubmitAppeal;

    public InternalRecordOutOfTimeDecisionLetterTemplate(
        @Value("${internalOutOfTimeDecisionLetter.templateName}") String templateName,
        @Value("${appellantDaysToWait.letter.afterSubmission}") int daysAfterSubmitAppeal,

        CustomerServicesProvider customerServicesProvider,
        SystemDateProvider systemDateProvider) {
        this.templateName = templateName;
        this.customerServicesProvider = customerServicesProvider;
        this.systemDateProvider = systemDateProvider;
        this.daysAfterSubmitAppeal = daysAfterSubmitAppeal;
    }

    @Override
    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();
        final String dueDate = systemDateProvider.dueDate(daysAfterSubmitAppeal);

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForRendering(LocalDate.now().toString(), DOCUMENT_DATE_FORMAT));
        fieldValues.put("fourWeeksAfterSubmitDate", dueDate);


        List<String> appellantAddress = new ArrayList<>(hasBeenSubmittedByAppellantInternalCase(asylumCase) ?
                getAppellantAddressInCountryOrOoc(asylumCase) : getLegalRepAddressInCountryOrOoc(asylumCase));

        for (int i = 0; i < appellantAddress.size(); i++) {
            fieldValues.put("address_line_" + (i + 1), appellantAddress.get(i));
        }
        return fieldValues;
    }
}
