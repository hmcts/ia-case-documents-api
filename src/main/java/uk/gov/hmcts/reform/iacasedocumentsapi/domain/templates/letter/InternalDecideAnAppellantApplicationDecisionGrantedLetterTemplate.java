package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.WhatHappensNextContentUtils.getWhatHappensNextContent;
import static uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.MakeAnApplicationService.APPLICATION_DECISION_REASON;
import static uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.MakeAnApplicationService.APPLICATION_TYPE;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
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
    private final UserDetailsProvider userDetailsProvider;



    public InternalDecideAnAppellantApplicationDecisionGrantedLetterTemplate(
            @Value("${internalDecideAnAppellantApplicationDecisionGrantedLetter.templateName}") String templateName,
            DateProvider dateProvider,
            CustomerServicesProvider customerServicesProvider,
            MakeAnApplicationService makeAnApplicationService,
            UserDetailsProvider userDetailsProvider) {
        this.templateName = templateName;
        this.dateProvider = dateProvider;
        this.customerServicesProvider = customerServicesProvider;
        this.makeAnApplicationService = makeAnApplicationService;
        this.userDetailsProvider = userDetailsProvider;

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
        Map<String, String> applicationPropeties = makeAnApplicationService.retrieveApplicationProperties(optionalMakeAnApplication);
        MakeAnApplicationTypes makeAnApplicationTypes = makeAnApplicationService.getApplicationTypes(applicationPropeties.get(APPLICATION_TYPE));
        final boolean applicationDecidedByLegalOfficer = userDetailsProvider.getUserDetails().isLegalOfficer();
        final boolean applicationDecidedByJudge = userDetailsProvider.getUserDetails().isJudge();

        String applicationDecidedBy = "";
        if (applicationDecidedByLegalOfficer) {
            applicationDecidedBy = "Legal Officer";
        } else if (applicationDecidedByJudge) {
            applicationDecidedBy = "Judge";
        }
        fieldValues.put("whatHappensNextContent", getWhatHappensNextContent(makeAnApplicationTypes, true));
        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(dateProvider.now()));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("applicationType", applicationPropeties.get(APPLICATION_TYPE));
        fieldValues.put("applicationReason", applicationPropeties.get(APPLICATION_DECISION_REASON));
        fieldValues.put("decisionMaker", applicationDecidedBy);

        return fieldValues;
    }

}
