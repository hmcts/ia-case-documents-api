package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.END_APPEAL_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.formatDateForRendering;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;

@Component
public class EndAppealAutomaticallyTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");

    public EndAppealAutomaticallyTemplate(
        @Value("${endAppealAutomatically.templateName}") String templateName
    ) {
        this.templateName = templateName;
    }

    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {

        final AsylumCase asylumCase = caseDetails.getCaseData();
        final Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("endAppealDate", formatDateForRendering(asylumCase.read(END_APPEAL_DATE, String.class).orElse(""), DOCUMENT_DATE_FORMAT));
        return fieldValues;
    }

}
