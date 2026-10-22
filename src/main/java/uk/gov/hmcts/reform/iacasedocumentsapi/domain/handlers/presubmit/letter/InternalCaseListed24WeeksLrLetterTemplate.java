package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.is24WeeksCase;

@Slf4j
@Component
public class InternalCaseListed24WeeksLrLetterTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private final StringProvider stringProvider;

    public InternalCaseListed24WeeksLrLetterTemplate(
        @Value("${internalCaseListedLetter24Weeks.templateName}") String templateName,
        CustomerServicesProvider customerServicesProvider,
        StringProvider stringProvider) {
        this.templateName = templateName;
        this.customerServicesProvider = customerServicesProvider;
        this.stringProvider = stringProvider;
    }

    @Override
    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        Map<String, Object> letterFieldValues = Stf24WeeksUtils.createInternalCaseListedLetterFieldValues(
                caseDetails,
                customerServicesProvider,
                stringProvider,
                true,
                is24WeeksCase(caseDetails.getCaseData())
        );
        log.info("Done 3 : {}", letterFieldValues);
        return letterFieldValues;
    }
}
