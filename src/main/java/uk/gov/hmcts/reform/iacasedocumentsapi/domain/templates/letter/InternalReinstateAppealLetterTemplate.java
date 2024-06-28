package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class InternalReinstateAppealLetterTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private static final String reinstateAppealAdaFormName = "IAFT-ADA4: Make an application – Accelerated detained appeal (ADA)";
    private static final String reinstateAppealNonAdaFormName = "IAFT-DE4: Make an application – Detained appeal";
    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    public InternalReinstateAppealLetterTemplate(
        @Value("${internalDetainedReinstateAppealLetter.templateName}") String templateName,
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

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("reinstatedDecisionMaker", asylumCase.read(REINSTATED_DECISION_MAKER, String.class).orElse(""));
        fieldValues.put("reinstateAppealDate", formatDateForRendering(asylumCase.read(REINSTATE_APPEAL_DATE, String.class).orElse(""), DOCUMENT_DATE_FORMAT));
        fieldValues.put("reinstateAppealReason", asylumCase.read(REINSTATE_APPEAL_REASON, String.class).orElse(""));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));
        fieldValues.put("formName", getFormName(asylumCase));
        return fieldValues;
    }

    private String getFormName(AsylumCase asylumCase) {
        return isAcceleratedDetainedAppeal(asylumCase) ? reinstateAppealAdaFormName : reinstateAppealNonAdaFormName;
    }

}
