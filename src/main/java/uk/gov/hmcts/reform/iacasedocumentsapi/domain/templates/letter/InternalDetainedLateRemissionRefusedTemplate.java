package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.REMISSION_DECISION_REASON;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

@Component
public class InternalDetainedLateRemissionRefusedTemplate implements DocumentTemplate<AsylumCase> {

    // Template field names
    private static final String DATE_LETTER_SENT = "dateLetterSent";
    private static final String DECISION_REASON = "remissionDecisionReason";
    private static final String CUSTOMER_SERVICES_TELEPHONE = "customerServicesTelephone";
    private static final String CUSTOMER_SERVICES_EMAIL = "customerServicesEmail";

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private final SystemDateProvider systemDateProvider;

    public InternalDetainedLateRemissionRefusedTemplate(
            @Value("${internalDetainedLateRemissionRefusedLetter.templateName}") String templateName,
            CustomerServicesProvider customerServicesProvider,
            SystemDateProvider systemDateProvider
    ) {
        this.templateName = templateName;
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

        fieldValues.put(DATE_LETTER_SENT, formatDateForNotificationAttachmentDocument(LocalDate.now()));
        fieldValues.put(DECISION_REASON, asylumCase.read(REMISSION_DECISION_REASON));
        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put(CUSTOMER_SERVICES_TELEPHONE, customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put(CUSTOMER_SERVICES_EMAIL, customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));

        return fieldValues;
    }
}
