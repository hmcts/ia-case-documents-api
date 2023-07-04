package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getDirectionDueDate;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class HoReviewEvidenceLetterTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final DateProvider dateProvider;
    private final CustomerServicesProvider customerServicesProvider;

    public HoReviewEvidenceLetterTemplate(
            @Value("${hoReviewEvidenceLetter.templateName}") String templateName,
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

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.put("hmcts", "[userImage:hmcts.png]");
        fieldValues.put("dateLetterSent", getFormattedDate(dateProvider.now()));
        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("directionDueDate", getFormattedDate(LocalDate.parse(getDirectionDueDate(asylumCase, DirectionTag.RESPONDENT_REVIEW))));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalAdaCustomerServicesTelephone());
        fieldValues.put("ADAemail", customerServicesProvider.getInternalAdaCustomerServicesEmail());
        return fieldValues;
    }

    private String getFormattedDate(LocalDate localDate) {
        return formatDateForNotificationAttachmentDocument(localDate);
    }
}
