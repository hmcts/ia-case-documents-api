package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.WhatHappensNextContentUtils.getWhatHappensNextContent;

import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplicationTypes;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.MakeAnApplicationService;

@Component
public class InternalDecideAnAppellantApplicationDecisionGrantedLetterTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final DateProvider dateProvider;
    private final CustomerServicesProvider customerServicesProvider;
    private final MakeAnApplicationService makeAnApplicationService;


    public InternalDecideAnAppellantApplicationDecisionGrantedLetterTemplate(
            @Value("${internalDecideAnAppellantApplicationDecisionGrantedLetter.templateName}") String templateName,
            DateProvider dateProvider,
            CustomerServicesProvider customerServicesProvider,
            MakeAnApplicationService makeAnApplicationService) {
        this.templateName = templateName;
        this.dateProvider = dateProvider;
        this.customerServicesProvider = customerServicesProvider;
        this.makeAnApplicationService = makeAnApplicationService;
    }

    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
            CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();
        final Map<String, Object> fieldValues = new HashMap<>();

        Optional<MakeAnApplication> optionalMakeAnApplication = makeAnApplicationService.getMakeAnApplication(asylumCase, true);

        String applicationType = "";
        String applicationDecision = "";
        String applicationDecisionReason = "No reason given";
        if (optionalMakeAnApplication.isPresent()) {
            MakeAnApplication makeAnApplication = optionalMakeAnApplication.get();
            applicationType = makeAnApplication.getType();
            applicationDecision = makeAnApplication.getDecision();
            applicationDecisionReason = makeAnApplication.getDecisionReason();
        }

        Optional<MakeAnApplicationTypes> optionalApplicationType = MakeAnApplicationTypes.from(applicationType);
        MakeAnApplicationTypes makeAnApplicationTypes;
        if (optionalApplicationType.isPresent()) {
            makeAnApplicationTypes = optionalApplicationType.get();
        } else {
            throw new IllegalStateException("Application type could not be parsed");
        }

        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(dateProvider.now()));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("applicationType", applicationType);
        fieldValues.put("applicationReason", applicationDecisionReason);
        fieldValues.put("whatHappensNextContent", getWhatHappensNextContent(makeAnApplicationTypes, true));

        return fieldValues;
    }

}