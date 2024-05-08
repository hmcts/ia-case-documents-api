package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DueDateService;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class InternalDetainedDecisionsAndReasonsAllowedLetterTemplate implements DocumentTemplate<AsylumCase> {


    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private final DueDateService dueDateService;
    private final int ftpaDueInWorkingDays;
    private final int ftpaDueInCalendarDays;

    public InternalDetainedDecisionsAndReasonsAllowedLetterTemplate(
        @Value("${internalDetainedDecisionsAndReasonsAllowedLetter.templateName}") String templateName,
        CustomerServicesProvider customerServicesProvider,
        DueDateService dueDateService,
        @Value("${internalDetainedDecisionsAndReasonsAllowedLetter.ftpaDueInWorkingDays}") int ftpaDueInWorkingDays,
        @Value("${internalDetainedDecisionsAndReasonsAllowedLetter.ftpaDueCalendarDays}") int ftpaDueInCalendarDays

    ) {
        this.templateName = templateName;
        this.customerServicesProvider = customerServicesProvider;
        this.dueDateService = dueDateService;
        this.ftpaDueInWorkingDays = ftpaDueInWorkingDays;
        this.ftpaDueInCalendarDays = ftpaDueInCalendarDays;
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
        fieldValues.put("ADAemail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));
        LocalDate dueDate = isAcceleratedDetainedAppeal(asylumCase)
                ? dueDateService.calculateDueDate(ZonedDateTime.now(), ftpaDueInWorkingDays).toLocalDate()
                : LocalDate.now().plusDays(ftpaDueInCalendarDays);
        fieldValues.put("ftpaDueDate", formatDateForNotificationAttachmentDocument(dueDate));

        return fieldValues;
    }
}
