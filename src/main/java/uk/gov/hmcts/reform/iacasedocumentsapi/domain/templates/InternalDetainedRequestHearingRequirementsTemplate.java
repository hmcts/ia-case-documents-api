package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DueDateService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

@Component
public class InternalDetainedRequestHearingRequirementsTemplate implements DocumentTemplate<AsylumCase>  {

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private final DueDateService dueDateService;

    public InternalDetainedRequestHearingRequirementsTemplate(
            @Value("${internalDetainedRequestHearingRequirements.templateName}") String templateName,
            CustomerServicesProvider customerServicesProvider,
            DueDateService dueDateService) {
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
        String hearingRequirementsSubmissionDeadline = getHearingRequirementsSubmissionDeadline(asylumCase);
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));
        fieldValues.put("hearingReqSubmissionDeadline", hearingRequirementsSubmissionDeadline);
        fieldValues.put("directionDueDate", hearingRequirementsSubmissionDeadline);

        return fieldValues;
    }

    private String getHearingRequirementsSubmissionDeadline(AsylumCase asylumCase) {
        LocalDate directionDueDate = LocalDate.parse(getDirectionDueDate(asylumCase, DirectionTag.REQUEST_RESPONSE_REVIEW));
        return formatDateForNotificationAttachmentDocument(dueDateService.calculateDueDate(directionDueDate.atStartOfDay(ZoneOffset.UTC), 5).toLocalDate());
    }

}