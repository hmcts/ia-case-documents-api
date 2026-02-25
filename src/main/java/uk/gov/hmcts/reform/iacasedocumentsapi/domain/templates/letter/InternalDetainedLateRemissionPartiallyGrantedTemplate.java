package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.AMOUNT_REMITTED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.REMISSION_DECISION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.REMISSION_DECISION_REASON;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionDecision.PARTIALLY_APPROVED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.convertAsylumCaseFeeValue;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

@Component
public class InternalDetainedLateRemissionPartiallyGrantedTemplate implements DocumentTemplate<AsylumCase> {

    // Template field names
    private static final String DATE_LETTER_SENT = "dateLetterSent";
    private static final String REFUND_AMOUNT = "refundAmount";
    private static final String ONLINE_CASE_REF_NUMBER = "onlineCaseRefNumber";
    private static final String REFUND_REASON = "refundReasons";
    private static final String DAYS_AFTER_REMISSION_DECISION = "daysAfterRemissionDecision";
    private static final String CUSTOMER_SERVICES_TELEPHONE = "customerServicesTelephone";
    private static final String CUSTOMER_SERVICES_EMAIL = "customerServicesEmail";

    private final String templateName;
    private final int daysAfterRemissionDecision;
    private final CustomerServicesProvider customerServicesProvider;
    private final SystemDateProvider systemDateProvider;

    public InternalDetainedLateRemissionPartiallyGrantedTemplate(
            @Value("${internalDetainedLateRemissionPartiallyGrantedLetter.templateName}") String templateName,
            @Value("${appellantDaysToWait.letter.afterRemissionDecision}") int daysAfterRemissionDecision,
            CustomerServicesProvider customerServicesProvider,
            SystemDateProvider systemDateProvider
    ) {
        this.templateName = templateName;
        this.daysAfterRemissionDecision = daysAfterRemissionDecision;
        this.customerServicesProvider = customerServicesProvider;
        this.systemDateProvider = systemDateProvider;
    }

    @Override
    public String getName() {
        return templateName;
    }

    @Override
    public Map<String, Object> mapFieldValues(CaseDetails<AsylumCase> caseDetails) {
        final AsylumCase asylumCase = caseDetails.getCaseData();
        final Map<String, Object> fieldValues = new HashMap<>();

        RemissionDecision remissionDecision = getRemissionDecision(asylumCase);
        String refundAmount = refundAmount(asylumCase, remissionDecision);

        fieldValues.put(DATE_LETTER_SENT, formatDateForNotificationAttachmentDocument(LocalDate.now()));
        fieldValues.put(REFUND_AMOUNT, refundAmount);
        fieldValues.put(REFUND_REASON, asylumCase.read(REMISSION_DECISION_REASON));
        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put(ONLINE_CASE_REF_NUMBER, asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY));
        fieldValues.put(DAYS_AFTER_REMISSION_DECISION, systemDateProvider.dueDate(daysAfterRemissionDecision));
        fieldValues.put(CUSTOMER_SERVICES_TELEPHONE, customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put(CUSTOMER_SERVICES_EMAIL, customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));

        return fieldValues;
    }

    private RemissionDecision getRemissionDecision(AsylumCase asylumCase) {
        return asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
                .orElseThrow(() -> new IllegalStateException("Remission decision is not set for case"));
    }

    private String refundAmount(AsylumCase asylumCase, RemissionDecision remissionDecision) {
        if (remissionDecision == PARTIALLY_APPROVED) {
            return convertAsylumCaseFeeValue(
                    asylumCase.read(AMOUNT_REMITTED, String.class).orElse(""));
        }
        return "";
    }

}
