package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.PAID_AMOUNT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

@Component
public class InternalDetMarkAsPaidLetterTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final DateProvider dateProvider;
    private final CustomerServicesProvider customerServicesProvider;

    public InternalDetMarkAsPaidLetterTemplate(
            @Value("${internalDetainedMarkAppealPaid.templateName}") String templateName,
            DateProvider dateProvider,
            CustomerServicesProvider customerServicesProvider) {
        this.templateName = templateName;
        this.dateProvider = dateProvider;
        this.customerServicesProvider = customerServicesProvider;
    }

    public String getName() {

        return templateName;
    }

    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.put("dateLetterSent", getFormattedDate(dateProvider.now()));
        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("ccdReferenceNumber", asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("feeBeforeRemission", String.valueOf(getFeeBeforeRemission(asylumCase)));
        fieldValues.put("feeRemission", String.valueOf(getFeeRemission(asylumCase)));
        String paidAmount = asylumCase.read(PAID_AMOUNT, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException("Paid amount is not present"));
        fieldValues.put("totalAmountToPay", String.valueOf(Double.parseDouble(paidAmount) / 100));
        return fieldValues;
    }

    private String getFormattedDate(LocalDate localDate) {
        return formatDateForNotificationAttachmentDocument(localDate);
    }
}
