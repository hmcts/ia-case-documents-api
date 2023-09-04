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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DueDateService;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class InternalAppellantFtpaDecidedRefusedTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final DateProvider dateProvider;
    private final CustomerServicesProvider customerServicesProvider;
    private final DueDateService dueDateService;
    private final int adaDueInWorkingDays;
    private final int nonAdaDueInCalendarDays;


    public InternalAppellantFtpaDecidedRefusedTemplate(
        @Value("${internalAppellantFtpaDecidedRefusedLetter.templateName}") String templateName,
        @Value("${internalAppellantFtpaDecidedRefusedLetter.ftpaAdaDueWorkingDays}") int adaDueInWorkingDays,
        @Value("${internalAppellantFtpaDecidedRefusedLetter.ftpaNonAdaDueCalendarDays}") int nonAdaDueInCalendarDays,
        DateProvider dateProvider,
        CustomerServicesProvider customerServicesProvider,
        DueDateService dueDateService) {
        this.templateName = templateName;
        this.adaDueInWorkingDays = adaDueInWorkingDays;
        this.nonAdaDueInCalendarDays = nonAdaDueInCalendarDays;
        this.dateProvider = dateProvider;
        this.customerServicesProvider = customerServicesProvider;
        this.dueDateService = dueDateService;
    }

    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();
        final Map<String, Object> fieldValues = new HashMap<>();

        LocalDate dueDate = isAcceleratedDetainedAppeal(asylumCase)
            ? dueDateService.calculateDueDate(ZonedDateTime.now(), adaDueInWorkingDays).toLocalDate()
            : LocalDate.now().plusDays(nonAdaDueInCalendarDays);

        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(dateProvider.now()));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("utApplicationDeadline", formatDateForNotificationAttachmentDocument(dueDate));

        return fieldValues;
    }
}
