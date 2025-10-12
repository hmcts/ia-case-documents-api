package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ApplicantType.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.FTPA_APPLICANT_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ApplicantType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class InternalPrisonOrIrcAppealDisposedUnderRule31Or32FtpaTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;

    public InternalPrisonOrIrcAppealDisposedUnderRule31Or32FtpaTemplate(
            @Value("${internalDetainedIrcPrisonAppealDisposedUnderRule31Or32Ftpa.templateName}") String templateName,
            CustomerServicesProvider customerServicesProvider
    ) {
        this.templateName = templateName;
        this.customerServicesProvider = customerServicesProvider;
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
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("ftpaDisposedReason", getDisposedReason(asylumCase));
        fieldValues.put("applicant", getApplicantType(asylumCase).equals(APPELLANT) ? "your" : "the Home Office's");

        return fieldValues;
    }

    private ApplicantType getApplicantType(AsylumCase asylumCase) {
        return asylumCase
            .read(FTPA_APPLICANT_TYPE, ApplicantType.class)
            .orElseThrow(() -> new IllegalStateException("ftpaApplicantType is not present"));
    }

    private String getDisposedReason(AsylumCase asylumCase) {
        return getApplicantType(asylumCase).equals(APPELLANT)
            ? asylumCase.read(FTPA_APPELLANT_DECISION_REMADE_RULE_32_TEXT, String.class).orElseThrow(() -> new IllegalStateException("ftpaAppellantDecisionRemadeRule32Text is not present"))
            : asylumCase.read(FTPA_RESPONDENT_DECISION_REMADE_RULE_32_TEXT, String.class).orElseThrow(() -> new IllegalStateException("ftpaRespondentDecisionRemadeRule32Text is not present"));
    }

}
