package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplicationTypes;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.WhatHappensNextContentUtilsWithDecision;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

@Component
public class HomeOfficeApplicationDecidedLetterTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final int daysAfterApplicationDecisionInCountry;
    private final CustomerServicesProvider customerServicesProvider;
    private final SystemDateProvider systemDateProvider;

    public HomeOfficeApplicationDecidedLetterTemplate(
            @Value("${homeOfficeApplicationDecidedLetter.templateName}") String templateName,
            @Value("${appellantDaysToWait.letter.afterRespondentApplicationDecided.inCountry}") int daysAfterApplicationDecisionInCountry,
            CustomerServicesProvider customerServicesProvider,
            SystemDateProvider systemDateProvider
    ) {
        this.templateName = templateName;
        this.daysAfterApplicationDecisionInCountry = daysAfterApplicationDecisionInCountry;
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
        final MakeAnApplication application = getDecidedApplication(asylumCase);
        final String dueDate = systemDateProvider.dueDate(daysAfterApplicationDecisionInCountry);
        final String nextSteps = WhatHappensNextContentUtilsWithDecision.getWhatHappensNextContent(
            MakeAnApplicationTypes.from(application.getType()).orElseThrow(() -> new IllegalStateException("Invalid MakeAnApplicationType")),
            false,
            application.getDecision(),
            dueDate
        );

        if (nextSteps.equals("Unknown")) {
            throw new IllegalStateException("Invalid MakeAnApplicationType: Couldn't find next steps.");
        }

        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));
        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("decision", application.getDecision().equals("Granted") ? "grant" : "refuse");
        fieldValues.put("applicationType", application.getType());
        fieldValues.put("applicationDate", application.getDate());
        fieldValues.put("applicationReason", application.getDecisionReason());
        fieldValues.put("nextSteps", nextSteps);
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));

        return fieldValues;
    }

}
