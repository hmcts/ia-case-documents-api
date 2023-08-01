package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class InternalDetainedDecisionsAndReasonsDismissedLetterTemplate implements DocumentTemplate<AsylumCase> {


    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private final int ftpaDueCalendarDays;

    public InternalDetainedDecisionsAndReasonsDismissedLetterTemplate(
            @Value("${internalDetainedDecisionsAndReasonsDismissedLetter.templateName}") String templateName,
            CustomerServicesProvider customerServicesProvider,
            @Value("${internalDetainedDecisionsAndReasonsDismissedLetter.ftpaDueCalendarDays}") int ftpaDueCalendarDays

    ) {
        this.templateName = templateName;
        this.customerServicesProvider = customerServicesProvider;
        this.ftpaDueCalendarDays = ftpaDueCalendarDays;
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
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("ADAemail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));
        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("ftpaDueDate", formatDateForNotificationAttachmentDocument(LocalDate.now().plusDays(ftpaDueCalendarDays)));

        return fieldValues;
    }
}
