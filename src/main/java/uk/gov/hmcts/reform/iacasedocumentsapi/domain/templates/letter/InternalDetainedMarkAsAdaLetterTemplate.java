package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_SUBMISSION_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.REASON_APPEAL_MARKED_AS_ADA;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.time.ZoneOffset;
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
public class InternalDetainedMarkAsAdaLetterTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private final DueDateService dueDateService;
    private final int responseToTribunalDueInWorkingDays = 13;
    private final int homeOfficeResponseDueInWorkingDays = 15;

    public InternalDetainedMarkAsAdaLetterTemplate(
            @Value("${internalMarkAppealAsAda.templateName}") String templateName,
            CustomerServicesProvider customerServicesProvider,
            DueDateService dueDateService) {
        this.templateName = templateName;
        this.customerServicesProvider = customerServicesProvider;
        this.dueDateService = dueDateService;
    }

    @Override
    public String getName() {
        return templateName;
    }

    @Override
    public Map<String, Object> mapFieldValues(
            CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));
        fieldValues.put("reason", asylumCase.read(REASON_APPEAL_MARKED_AS_ADA, String.class).orElse(""));
        fieldValues.put("responseDueDate", resolveResponseDueDate(asylumCase, responseToTribunalDueInWorkingDays));
        fieldValues.put("hoReviewAppealDueDate", resolveResponseDueDate(asylumCase, homeOfficeResponseDueInWorkingDays));
        return fieldValues;
    }

    private String resolveResponseDueDate(AsylumCase asylumCase, int dueDays) {
        LocalDate appealSubmissionDate = LocalDate.parse(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class)
                .orElseThrow(() -> new IllegalStateException("Appeal submission date is missing")));

        return formatDateForNotificationAttachmentDocument(dueDateService
                .calculateDueDate(appealSubmissionDate.atStartOfDay(ZoneOffset.UTC), dueDays)
                .toLocalDate());
    }
}
