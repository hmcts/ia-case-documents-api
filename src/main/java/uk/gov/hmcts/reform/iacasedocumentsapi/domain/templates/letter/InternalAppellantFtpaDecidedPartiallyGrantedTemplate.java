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
public class InternalAppellantFtpaDecidedPartiallyGrantedTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final DateProvider dateProvider;
    private final CustomerServicesProvider customerServicesProvider;
    private final DueDateService dueDateService;
    private final int adaDueWorkingDays;
    private final int nonAdaDueInCalendarDays;

    public InternalAppellantFtpaDecidedPartiallyGrantedTemplate(
        @Value("${internalAppellantFtpaDecidedPartiallyGrantedLetter.templateName}") String templateName,
        DateProvider dateProvider,
        CustomerServicesProvider customerServicesProvider,
        DueDateService dueDateService,
        @Value("${internalAppellantFtpaDecidedPartiallyGrantedLetter.ftpaAdaDueWorkingDays}") int adaDueWorkingDays,
        @Value("${internalAppellantFtpaDecidedPartiallyGrantedLetter.ftpaNonAdaDueCalendarDays}") int nonAdaDueInCalendarDays) {
        this.templateName = templateName;
        this.dateProvider = dateProvider;
        this.customerServicesProvider = customerServicesProvider;
        this.dueDateService = dueDateService;
        this.adaDueWorkingDays = adaDueWorkingDays;
        this.nonAdaDueInCalendarDays = nonAdaDueInCalendarDays;
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
            ? dueDateService.calculateWorkingDaysDueDate(ZonedDateTime.now(), adaDueWorkingDays).toLocalDate()
            : LocalDate.now().plusDays(nonAdaDueInCalendarDays);

        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(dateProvider.now()));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("utApplicationDeadline", formatDateForNotificationAttachmentDocument(dueDate));

        return fieldValues;
    }
}
