package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealDecision;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DecisionAndReasons;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class InternalUpdateTribunalDecisionR31Template implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final DateTimeFormatter PARSING_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public InternalUpdateTribunalDecisionR31Template(
        @Value("${internalUpdateTribunalDecisionR31Letter.templateName}") String templateName,
        CustomerServicesProvider customerServicesProvider) {
        this.templateName = templateName;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final String originalUpdatedDecisionAndReasonsDate = getOriginalDateDocumentAndReasonsDocumentUploaded(asylumCase);

        if (originalUpdatedDecisionAndReasonsDate.isEmpty()) {
            throw new IllegalStateException("Original date for document and reasons is not available");
        }

        LocalDate documentDate = LocalDate.parse(originalUpdatedDecisionAndReasonsDate, DOCUMENT_DATE_FORMAT);
        LocalDate dueDateDetained = documentDate.plusDays(14);
        String formattedDueDate = dueDateDetained.format(DOCUMENT_DATE_FORMAT);

        String isAllowed = currentDecisionCheck(asylumCase);

        String dynamicContentBasedOnDecision = "";
        String newDecision = isAllowed.equals("allowed") ? "dismissed" : "allowed";
        boolean firstCheck = asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class).map(list -> list.getValue().getLabel().contains("Yes")).orElse(false);
        boolean secondCheck = asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false);

        final String latestUpdatedDecisionAndReaonsDate = getLatestDateDocumentAndReasonsDocumentUploaded(asylumCase);

        if (latestUpdatedDecisionAndReaonsDate.isEmpty() && secondCheck) {
            throw new IllegalStateException("Latest date for document and reasons is not available");
        }

        if (firstCheck && !secondCheck) {
            dynamicContentBasedOnDecision = "The Tribunal made a mistake recording your appeal decision. \n\n Your decision was recorded as " +
                                            isAllowed + " but should have been recorded as " + newDecision + "." + " \n\n The Tribunal has fixed this mistake and your appeal decision has been correctly recorded as " + newDecision + "." + " \n\n If you disagree with the appeal decision, you have until " + formattedDueDate + " to ask for permission to appeal to the Upper Tribunal.";

        } else if (!firstCheck && secondCheck) {
            dynamicContentBasedOnDecision = "The Tribunal entered some wrong information in the Decision and Reasons document for this appeal. \n\n The Tribunal has created a <b>new Decisions and Reasons document</b> which includes the correct information. This document should be given to you along with this letter. ";

        } else if (firstCheck && secondCheck) {

            LocalDate newDocumentDate = LocalDate.parse(latestUpdatedDecisionAndReaonsDate, DOCUMENT_DATE_FORMAT);
            LocalDate dueDate = newDocumentDate.plusDays(14);
            String formattedNewDueDate = dueDate.format(DOCUMENT_DATE_FORMAT);

            dynamicContentBasedOnDecision = "The Tribunal made a mistake recording your appeal decision. \n\n Your decision was recorded as " + isAllowed + " but should have been recorded as " + newDecision + "." + " \n\n The Tribunal has fixed this mistake and your appeal decision has been correctly recorded as " + newDecision + "."
                                            + "\n\n The Tribunal also entered some wrong information in the Decision and Reasons document for this appeal. \n\n The Tribunal has created a <b>new Decisions and Reasons document</b> which includes the correct information. This document should be given to you along with this letter. "
                                            + "\n\n If you disagree with the appeal decision, you have until " + formattedNewDueDate + " to ask for permission to appeal to the Upper Tribunal. ";
        }

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));
        fieldValues.put("dynamicContentBasedOnDecision", dynamicContentBasedOnDecision);
        return fieldValues;
    }

    private String currentDecisionCheck(AsylumCase asylumCase) {
        return asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)
            .map(appealDecision -> appealDecision.equals(AppealDecision.ALLOWED) ? "allowed" : "dismissed")
            .orElse("No");
    }

    private String getLatestDateDocumentAndReasonsDocumentUploaded(AsylumCase asylumCase) {
        Optional<List<IdValue<DecisionAndReasons>>> correctedDecisionAndReasonsOptional =
            asylumCase.read(CORRECTED_DECISION_AND_REASONS);

        if (correctedDecisionAndReasonsOptional.isPresent()) {
            List<IdValue<DecisionAndReasons>> correctedDecisionAndReasons = correctedDecisionAndReasonsOptional.get();

            return correctedDecisionAndReasons.stream()
                .map(IdValue::getValue)
                .map(DecisionAndReasons::getDateDocumentAndReasonsDocumentUploaded)
                .filter(StringUtils::isNotEmpty)
                .findFirst()
                .map(date -> LocalDate.parse(date, PARSING_DATE_FORMAT).format(DOCUMENT_DATE_FORMAT))
                .orElse(StringUtils.EMPTY);
        }
        return StringUtils.EMPTY;
    }

    private String getOriginalDateDocumentAndReasonsDocumentUploaded(AsylumCase asylumCase) {
        String sendDandRDate = asylumCase.read(SEND_DECISIONS_AND_REASONS_DATE, String.class)
            .orElseThrow(() -> new IllegalStateException("Send Decisions and reasons date due is not present"));

        LocalDate parsedDate = LocalDate.parse(sendDandRDate, PARSING_DATE_FORMAT);
        return parsedDate.format(DOCUMENT_DATE_FORMAT);
    }
}