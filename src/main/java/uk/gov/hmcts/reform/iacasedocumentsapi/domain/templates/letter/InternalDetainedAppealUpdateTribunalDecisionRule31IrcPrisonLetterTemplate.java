package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

@Component
public class InternalDetainedAppealUpdateTribunalDecisionRule31IrcPrisonLetterTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final int daysUpdateTribunalDecisionDone;
    private final CustomerServicesProvider customerServicesProvider;
    private final SystemDateProvider systemDateProvider;

    public InternalDetainedAppealUpdateTribunalDecisionRule31IrcPrisonLetterTemplate(
            @Value("${internalDetainedAppealUpdateTribunalDecisionRule31IrcPrisonLetter.templateName}") String templateName,
            @Value("${appellantDaysToWait.letter.updateTribunalDecisionDone}") int daysUpdateTribunalDecisionDone,
            CustomerServicesProvider customerServicesProvider,
            SystemDateProvider systemDateProvider
    ) {
        this.templateName = templateName;
        this.daysUpdateTribunalDecisionDone = daysUpdateTribunalDecisionDone;
        this.customerServicesProvider = customerServicesProvider;
        this.systemDateProvider = systemDateProvider;
    }

    @Override
    public String getName() {
        return templateName;
    }

    @Override
    public Map<String, Object> mapFieldValues(CaseDetails<AsylumCase> caseDetails) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));
        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("updateTribunalDecisionDoneDate", systemDateProvider.dueDate(daysUpdateTribunalDecisionDone));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));

        return fieldValues;
    }

}
