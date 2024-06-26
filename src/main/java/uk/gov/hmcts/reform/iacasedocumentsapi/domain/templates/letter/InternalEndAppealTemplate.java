package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class InternalEndAppealTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private final String adaFormName = "IAFT-ADA4: Make an application – Accelerated detained appeal (ADA)";
    private final String nonAdaFormName = "IAFT-DE4: Make an application – Detained appeal";

    public InternalEndAppealTemplate(
            @Value("${internalDetainedEndAppeal.templateName}") String templateName,
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
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));
        fieldValues.put("decisionMaker", caseDetails.getCaseData().read(END_APPEAL_APPROVER_TYPE, String.class).orElse(""));
        fieldValues.put("endAppealDate", formatDateForNotificationAttachmentDocument(LocalDate.parse(asylumCase.read(END_APPEAL_DATE, String.class)
                .orElseThrow(() -> new IllegalStateException("End appeal date is missing")))));
        fieldValues.put("formName", resolveFormName(asylumCase));
        return fieldValues;
    }

    private String resolveFormName(AsylumCase asylumCase) {
        return isAcceleratedDetainedAppeal(asylumCase)
                ? adaFormName
                : nonAdaFormName;
    }
}
