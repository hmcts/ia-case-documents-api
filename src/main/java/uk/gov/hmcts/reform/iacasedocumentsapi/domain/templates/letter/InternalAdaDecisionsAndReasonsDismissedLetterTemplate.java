package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DueDateService;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class InternalAdaDecisionsAndReasonsDismissedLetterTemplate implements DocumentTemplate<AsylumCase> {


    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private final DueDateService dueDateService;
    private final int ftpaDueInWorkingDays;

    public InternalAdaDecisionsAndReasonsDismissedLetterTemplate(
            @Value("${internalAdaDecisionsAndReasonsDismissedLetter.templateName}") String templateName,
            CustomerServicesProvider customerServicesProvider,
            DueDateService dueDateService,
            @Value("${internalAdaDecisionsAndReasonsDismissedLetter.ftpaDueInWorkingDays}") int ftpaDueInWorkingDays

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

        List<Direction> requestBuildCaseDirectionList = getCaseDirectionsBasedOnTag(asylumCase, DirectionTag.REQUEST_CASE_BUILDING);
        List<Direction> respondendReviewDirectionList = getCaseDirectionsBasedOnTag(asylumCase, DirectionTag.RESPONDENT_REVIEW);

        if (requestBuildCaseDirectionList.isEmpty()) {
            throw new RequiredFieldMissingException("No requestBuildCase directions found");
        }

        if (requestBuildCaseDirectionList.size() > 1) {
            throw new IllegalStateException("More than 1 requestCaseBuilding direction");
        }

        if (respondendReviewDirectionList.isEmpty()) {
            throw new RequiredFieldMissingException("No respondent review directions found");
        }

        if (respondendReviewDirectionList.size() > 1) {
            throw new IllegalStateException("More than 1 respondent review direction");
        }

        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("ADAemail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));
        fieldValues.putAll(getAppellantPersonalisation(asylumCase));

        fieldValues.put("ftpaDueDate", formatDateForNotificationAttachmentDocument(dueDateService
                .calculateDueDate(ZonedDateTime.now(), ftpaDueInWorkingDays)
                .toLocalDate()));

        fieldValues.put("responseDueDate", formatDateForNotificationAttachmentDocument(LocalDate.parse(getDirectionDueDate(asylumCase, DirectionTag.REQUEST_CASE_BUILDING))));
        fieldValues.put("hoReviewAppealDueDate", formatDateForNotificationAttachmentDocument(LocalDate.parse(getDirectionDueDate(asylumCase, DirectionTag.RESPONDENT_REVIEW))));

        return fieldValues;
    }
}
