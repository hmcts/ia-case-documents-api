package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_REFUSED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.FtpaDecisionOutcomeType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class InternalHoFtpaDecidedRefusedLetterTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private final String refused = "refused";
    private final String notAdmitted = "not admitted";

    public InternalHoFtpaDecidedRefusedLetterTemplate(
            @Value("${internalDetainedHoFtpaDecidedRefusedLetter.templateName}") String templateName,
            CustomerServicesProvider customerServicesProvider) {
        this.templateName = templateName;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getName() {
        return templateName;
    }

    @Override
    public Map<String, Object> mapFieldValues(
            CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final Map<String, Object> fieldValues = new HashMap<>(getAppellantPersonalisation(asylumCase));
        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));
        fieldValues.put("refused", asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)
                .map(it -> it.equals(FTPA_REFUSED) ? refused : notAdmitted)
                .orElseThrow(() -> new IllegalStateException("Judge decision 'refused' or 'not admitted' must be present")));
        return fieldValues;
    }
}
