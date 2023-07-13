package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getDirectionDueDate;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DueDateService;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class UploadAppealResponseMaintainedDecisionLetterTemplate implements DocumentTemplate<AsylumCase> {

    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");
    private final String templateName;
    private final DateProvider dateProvider;
    private final CustomerServicesProvider customerServicesProvider;
    private final DueDateService dueDateService;

    public UploadAppealResponseMaintainedDecisionLetterTemplate(
            @Value("${uploadAppealResponseMaintainedLetter.templateName}") String templateName,
            DateProvider dateProvider,
            CustomerServicesProvider customerServicesProvider,
            DueDateService dueDateService) {
        this.templateName = templateName;
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

        fieldValues.put("hmcts", "[userImage:hmcts.png]");
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(dateProvider.now()));
        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("ADAemail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("suitabilityAssessmentHearingDate", getFormattedDate(getSaHearingDate(asylumCase)));
        fieldValues.put("hearingDate", getFormattedDate(getLocalDateForFieldValue(asylumCase, LIST_CASE_HEARING_DATE)));
        fieldValues.put("caseBuildingDueDate", formatDateForNotificationAttachmentDocument(LocalDate.parse(getDirectionDueDate(asylumCase, DirectionTag.REQUEST_CASE_BUILDING))));
        fieldValues.put("requestRespondentReviewDueDate", formatDateForNotificationAttachmentDocument(LocalDate.parse(getDirectionDueDate(asylumCase, DirectionTag.RESPONDENT_REVIEW))));
        return fieldValues;
    }

    private String getFormattedDate(LocalDate localDate) {
        return localDate.format(DOCUMENT_DATE_FORMAT);
    }

    private LocalDate getLocalDateForFieldValue(AsylumCase asylumCase, AsylumCaseDefinition field) {
        return LocalDateTime.parse(asylumCase.read(field, String.class).orElseThrow(() -> new RequiredFieldMissingException(field.toString() + " not found."))).toLocalDate();
    }

    private LocalDate getSaHearingDate(AsylumCase asylumCase) {
        LocalDate appealSubmissionDate = LocalDate.parse(asylumCase.read(AsylumCaseDefinition.APPEAL_SUBMISSION_DATE, String.class).orElseThrow(() -> new RequiredFieldMissingException("Appeal Submission Date is missing")));
        return dueDateService.calculateDueDate(appealSubmissionDate.atStartOfDay(ZoneOffset.UTC), 16).toLocalDate();
    }

}
