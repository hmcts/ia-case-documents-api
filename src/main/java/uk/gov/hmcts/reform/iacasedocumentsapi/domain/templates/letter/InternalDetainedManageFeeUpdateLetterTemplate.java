package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.FeeUpdateReason;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

@Component
public class InternalDetainedManageFeeUpdateLetterTemplate implements DocumentTemplate<AsylumCase> {


    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private final SystemDateProvider systemDateProvider;
    private final int afterManageFeeEvent;

    public InternalDetainedManageFeeUpdateLetterTemplate(
            @Value("${internalDetainedManageFeeUpdateLetter.templateName}") String templateName,
            @Value("${appellantDaysToWait.letter.afterManageFeeUpdate}") int afterManageFeeEvent,
            CustomerServicesProvider customerServicesProvider,
            SystemDateProvider systemDateProvider
    ) {
        this.templateName = templateName;
        this.customerServicesProvider = customerServicesProvider;
        this.systemDateProvider = systemDateProvider;
        this.afterManageFeeEvent = afterManageFeeEvent;
    }

    @Override
    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
            CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final String dueDate = systemDateProvider.dueDate(afterManageFeeEvent);

        String originalFeeTotal = asylumCase.read(AsylumCaseDefinition.FEE_AMOUNT_GBP, String.class).orElse("");
        String newFeeTotal = asylumCase.read(AsylumCaseDefinition.NEW_FEE_AMOUNT, String.class).orElse("");
        String feeDifference = calculateFeeDifference(originalFeeTotal, newFeeTotal);

        String feeUpdateReason = formatFeeUpdateReason(asylumCase.read(AsylumCaseDefinition.FEE_UPDATE_REASON, FeeUpdateReason.class)
                .orElseThrow(() -> new IllegalStateException("FeeUpdateReason is not present")));

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));
        fieldValues.put("originalFeeTotal", convertAsylumCaseFeeValue(asylumCase.read(AsylumCaseDefinition.FEE_AMOUNT_GBP, String.class).orElse("")));
        fieldValues.put("newFeeTotal", convertAsylumCaseFeeValue(asylumCase.read(AsylumCaseDefinition.NEW_FEE_AMOUNT, String.class).orElse("")));
        fieldValues.put("feeDifference", feeDifference);
        fieldValues.put("feeUpdateReasonSelected", feeUpdateReason);
        fieldValues.put("onlineCaseRefNumber", asylumCase.read(AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class).orElse(""));
        fieldValues.put("dueDate14Days", dueDate);

        return fieldValues;
    }

    public static String formatFeeUpdateReason(FeeUpdateReason feeUpdateReason) {
        String value = feeUpdateReason.getValue();
        return Arrays.stream(value.split("(?=[A-Z])"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}
