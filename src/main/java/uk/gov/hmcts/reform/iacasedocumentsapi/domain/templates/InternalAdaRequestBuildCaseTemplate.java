package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DueDateService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class InternalAdaRequestBuildCaseTemplate implements DocumentTemplate<AsylumCase> {

    private final int hearingSupportResponseDueInWorkingDays;
    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private final DueDateService dueDateService;


    public InternalAdaRequestBuildCaseTemplate(
            @Value("${internalAdaRequestBuildCaseDocument.hearingSupportResponseDueInWorkingDays}") int hearingSupportResponseDueInWorkingDays,
            @Value("${internalAdaRequestBuildCaseDocument.templateName}") String templateName,
            CustomerServicesProvider customerServicesProvider,
            DueDateService dueDateService
    ) {
        this.hearingSupportResponseDueInWorkingDays = hearingSupportResponseDueInWorkingDays;
        this.templateName = templateName;
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
        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));

        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));
        fieldValues.put("responseDueDate", formatDateForNotificationAttachmentDocument(LocalDate.parse(getDirectionDueDate(asylumCase, DirectionTag.REQUEST_CASE_BUILDING))));

        fieldValues.put("hearingSupportRequirementsDueDate", formatDateForNotificationAttachmentDocument(dueDateService
                .calculateDueDate(ZonedDateTime.now(), hearingSupportResponseDueInWorkingDays)
                .toLocalDate()));

        return fieldValues;
    }

}
