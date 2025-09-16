package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LAST_MODIFIED_DIRECTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

@Component
public class InternalDetainedResponderReviewIrcPrisonTemplate implements DocumentTemplate<AsylumCase> {


    private final String templateName;
    private final DateProvider dateProvider;
    private final CustomerServicesProvider customerServicesProvider;

    public InternalDetainedResponderReviewIrcPrisonTemplate(
            @Value("${internalDetainedRespondentReviewIrcPrisonNotificationLetter.templateName}") String templateName,
            DateProvider dateProvider,
            CustomerServicesProvider customerServicesProvider) {
        this.templateName = templateName;
        this.dateProvider = dateProvider;
        this.customerServicesProvider = customerServicesProvider;
    }

    public String getName() {

        return templateName;
    }

    public Map<String, Object> mapFieldValues(
            CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();
        String directionDueDate;
        Optional<Direction> lastDirectionOpt = asylumCase.read(LAST_MODIFIED_DIRECTION, Direction.class);

        directionDueDate = lastDirectionOpt.map(
                direction -> formatDateForNotificationAttachmentDocument(LocalDate.parse(direction.getDateDue()))
        ).orElse("");
        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(dateProvider.now()));
        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("directionDueDate", directionDueDate);
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)); // how to change this to customer services email? or is it already and just need to change text to "customer services email"
        return fieldValues;
    }
}


