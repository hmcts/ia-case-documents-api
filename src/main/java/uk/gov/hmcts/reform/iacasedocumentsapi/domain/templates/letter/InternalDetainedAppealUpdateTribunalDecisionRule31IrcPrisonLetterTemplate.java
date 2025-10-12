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
import java.util.Optional;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_DECISION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.UPDATED_APPEAL_DECISION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.UPDATE_TRIBUNAL_DECISION_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

@Component
public class InternalDetainedAppealUpdateTribunalDecisionRule31IrcPrisonLetterTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final int appealAfterTribunalDecision;
    private final CustomerServicesProvider customerServicesProvider;
    private final SystemDateProvider systemDateProvider;

    public InternalDetainedAppealUpdateTribunalDecisionRule31IrcPrisonLetterTemplate(
            @Value("${internalDetainedAppealUpdateTribunalDecisionRule31IrcPrisonLetter.templateName}") String templateName,
            @Value("${appellantDaysToWait.letter.appealAfterTribunalDecision}") int appealAfterTribunalDecision,
            CustomerServicesProvider customerServicesProvider,
            SystemDateProvider systemDateProvider
    ) {
        this.templateName = templateName;
        this.appealAfterTribunalDecision = appealAfterTribunalDecision;
        this.customerServicesProvider = customerServicesProvider;
        this.systemDateProvider = systemDateProvider;
    }

    @Override
    public String getName() {
        return templateName;
    }

    @Override
    public Map<String, Object> mapFieldValues(CaseDetails<AsylumCase> caseDetails, CaseDetails<AsylumCase> caseDetailsBefore) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final Map<String, Object> fieldValues = new HashMap<>();

        final String appealTribunalDecisionDeadlineDate = formatDateForNotificationAttachmentDocument(LocalDate.parse(asylumCase.read(UPDATE_TRIBUNAL_DECISION_DATE, String.class)
                        .orElseThrow(() -> new IllegalStateException("Update Tribunal Decision date is missing")))
                .plusDays(appealAfterTribunalDecision));

        final Optional<String> updatedAppealDecisionBefore = caseDetailsBefore.getCaseData().read(UPDATED_APPEAL_DECISION, String.class);
        final String oldDecision = updatedAppealDecisionBefore.isPresent() ? updatedAppealDecisionBefore.get() : caseDetailsBefore.getCaseData().read(APPEAL_DECISION, String.class)
                        .orElseThrow(() -> new IllegalStateException("Appeal Decision and Updated Appeal Decision are missing"));

        final String newDecision = asylumCase.read(UPDATED_APPEAL_DECISION, String.class)
                .orElseThrow(() -> new IllegalStateException("Updated Appeal Decision is missing"));

        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));
        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("appealTribunalDecisionDeadlineDate", appealTribunalDecisionDeadlineDate);
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("oldDecision", oldDecision);
        fieldValues.put("newDecision", newDecision);

        return fieldValues;
    }

}
