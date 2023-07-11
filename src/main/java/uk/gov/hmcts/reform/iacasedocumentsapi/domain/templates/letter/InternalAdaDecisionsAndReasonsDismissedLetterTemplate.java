package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

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

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getCaseDirectionsBasedOnTag;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

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

        Direction requestBuildCaseDirection = requestBuildCaseDirectionList.get(0);
        Direction respondendReviewDirection = respondendReviewDirectionList.get(0);

        fieldValues.put("hmcts", "[userImage:hmcts.png]");
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("ADAemail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));

        fieldValues.put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""));
        fieldValues.put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""));

        fieldValues.put("ftpaDueDate", formatDateForNotificationAttachmentDocument(dueDateService
                .calculateDueDate(ZonedDateTime.now(), ftpaDueInWorkingDays)
                .toLocalDate()));

        fieldValues.put("responseDueDate", formatDateForNotificationAttachmentDocument(LocalDate.parse(requestBuildCaseDirection.getDateDue())));
        fieldValues.put("hoReviewAppealDueDate", formatDateForNotificationAttachmentDocument(LocalDate.parse(respondendReviewDirection.getDateDue())));

        return fieldValues;
    }
}
