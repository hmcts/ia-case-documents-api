package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DirectionFinder;

@Component
public class InternalNonStandardDirectionLetterTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private final DirectionFinder directionFinder;

    public InternalNonStandardDirectionLetterTemplate(
        @Value("${internalNonStandardDirectionLetter.templateName}") String templateName,
        CustomerServicesProvider customerServicesProvider,
        DirectionFinder directionFinder) {
        this.templateName = templateName;
        this.customerServicesProvider = customerServicesProvider;
        this.directionFinder = directionFinder;
    }

    public String getName() {

        return templateName;
    }

    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();
        final Map<String, Object> fieldValues = new HashMap<>();

        Optional<Direction> direction = directionFinder.findFirst(asylumCase, DirectionTag.NONE);

        if (direction.isEmpty()) {
            throw new IllegalStateException("Could not find non-standard direction");

        }

        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));
        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("sendDirectionContent", direction.get().getExplanation());
        fieldValues.put("directionDueDate", formatDateForNotificationAttachmentDocument(LocalDate.parse(direction.get().getDateDue())));

        return fieldValues;
    }
}
