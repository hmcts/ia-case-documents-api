package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.helper;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.END_APPEAL_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.END_APPEAL_OUTCOME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.END_APPEAL_OUTCOME_REASON;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;


@Component
public class EndAppealTemplateHelper {

    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");

    private final CustomerServicesProvider customerServicesProvider;

    public EndAppealTemplateHelper(CustomerServicesProvider customerServicesProvider) {
        this.customerServicesProvider = customerServicesProvider;
    }

    public Map<String, Object> getCommonMapFieldValues(
            CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("outcomeOfAppeal", asylumCase.read(END_APPEAL_OUTCOME, String.class).orElse(""));
        fieldValues.put("reasonsOfOutcome", asylumCase.read(END_APPEAL_OUTCOME_REASON, String.class).orElse(""));
        fieldValues.put("endAppealDate", formatDateForRendering(asylumCase.read(END_APPEAL_DATE, String.class).orElse("")));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getCustomerServicesTelephone());
        fieldValues.put("customerServicesEmail", customerServicesProvider.getCustomerServicesEmail());

        return fieldValues;
    }

    private String formatDateForRendering(
            String date
    ) {
        if (!Strings.isNullOrEmpty(date)) {
            return LocalDate.parse(date).format(DOCUMENT_DATE_FORMAT);
        }

        return "";
    }

}
