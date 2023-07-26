package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getDirectionDueDate;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class InternalDetainedRequestBuildCaseTemplate implements DocumentTemplate<AsylumCase> {

    private final String nonAdaTemplateName;
    private final CustomerServicesProvider customerServicesProvider;


    public InternalDetainedRequestBuildCaseTemplate(
            @Value("${internalDetainedRequestBuildCaseDocument.templateName}") String nonAdaTemplateName,
            CustomerServicesProvider customerServicesProvider
    ) {
        this.nonAdaTemplateName = nonAdaTemplateName;
        this.customerServicesProvider = customerServicesProvider;
    }

    public String getName() {

        return nonAdaTemplateName;
    }

    public Map<String, Object> mapFieldValues(
            CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.put("hmcts", "[userImage:hmcts.png]");
        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));
        fieldValues.put("responseDueDate", formatDateForNotificationAttachmentDocument(LocalDate.parse(getDirectionDueDate(asylumCase, DirectionTag.REQUEST_CASE_BUILDING))));

        return fieldValues;
    }

}
