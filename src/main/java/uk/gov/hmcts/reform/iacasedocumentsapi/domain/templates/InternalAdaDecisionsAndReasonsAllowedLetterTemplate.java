package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class InternalAdaDecisionsAndReasonsAllowedLetterTemplate implements DocumentTemplate<AsylumCase> {


    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private final DueDateService dueDateService;
    private final int ftpaDueInWorkingDays;

    public InternalAdaDecisionsAndReasonsAllowedLetterTemplate(
        @Value("${internalAdaDecisionsAndReasonsAllowedLetter.templateName}") String templateName,
        CustomerServicesProvider customerServicesProvider,
        DueDateService dueDateService,
        @Value("${internalAdaDecisionsAndReasonsAllowedLetter.ftpaDueInWorkingDays}") int ftpaDueInWorkingDays

    ) {
        this.templateName = templateName;
        this.customerServicesProvider = customerServicesProvider;
        this.dueDateService = dueDateService;
        this.ftpaDueInWorkingDays = ftpaDueInWorkingDays;
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
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalAdaCustomerServicesTelephone());
        fieldValues.put("ADAemail", customerServicesProvider.getInternalAdaCustomerServicesEmail());
        fieldValues.put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));

        fieldValues.put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""));
        fieldValues.put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""));

        fieldValues.put("ftpaDueDate", formatDateForNotificationAttachmentDocument(dueDateService
            .calculateDueDate(ZonedDateTime.now(), ftpaDueInWorkingDays)
            .toLocalDate()));

        return fieldValues;
    }
}
