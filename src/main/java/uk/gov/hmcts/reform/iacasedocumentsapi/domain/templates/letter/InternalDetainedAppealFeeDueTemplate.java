package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;


@Component
public class InternalDetainedAppealFeeDueTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private final int calenderDaysToPayAppealFee = 14;

    public InternalDetainedAppealFeeDueTemplate(
            @Value("${internalDetainedAppealFeeDue.templateName}") String templateName,
            CustomerServicesProvider customerServicesProvider
    ) {
        this.templateName = templateName;
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

        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("ccdReferenceNumber", asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));
        fieldValues.putAll(getAppellantPersonalisation(asylumCase));

        fieldValues.put("deadlineDate", formatDateForNotificationAttachmentDocument(LocalDate.now().plusDays(calenderDaysToPayAppealFee)));

        Double feeAmountInPence = Double.parseDouble(asylumCase.read(FEE_AMOUNT_GBP, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException("Fee amount not found")));
        double feeAmountInPounds = feeAmountInPence / 100;
        double remissionAmount = getFeeRemission(asylumCase);
        fieldValues.put("feeBeforeRemission", feeAmountInPounds);
        fieldValues.put("feeRemission", remissionAmount);
        fieldValues.put("totalAmountToPay", feeAmountInPounds - remissionAmount);

        return fieldValues;
    }

    private double getFeeRemission(AsylumCase asylumCase) {
        Optional<RemissionType> remissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);
        if (remissionType.isEmpty()) {
            throw new RequiredFieldMissingException("Remission type not found");
        }

        if (remissionType.get().equals(RemissionType.NO_REMISSION)) {
            return 0;
        } else {
            // to do
            return 100;
        }
    }
}
