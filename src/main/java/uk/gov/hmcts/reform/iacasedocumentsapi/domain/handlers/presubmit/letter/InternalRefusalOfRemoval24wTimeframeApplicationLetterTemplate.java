package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REF_NUMBER_PAPER_J;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantAddressInCountryOrOoc;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getDecidedApplication;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.hasBeenSubmittedAsLegalRepresentedInternalCase;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

@Component
public class InternalRefusalOfRemoval24wTimeframeApplicationLetterTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;

    public InternalRefusalOfRemoval24wTimeframeApplicationLetterTemplate(
        @Value("${internalRefusalOfRemoval24wTimeframeApplicationLetter.templateName}") String templateName,
        CustomerServicesProvider customerServicesProvider) {
        this.templateName = templateName;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();
        final MakeAnApplication application = getDecidedApplication(asylumCase);

        final Map<String, Object> fieldValues = new HashMap<>(getAppellantPersonalisation(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));
        fieldValues.put("decisionMaker", application.getDecisionMaker());
        if ("Respondent".equals(application.getApplicantRole())) {
            fieldValues.put("whoseApplicationHeading", "A Home Office");
            fieldValues.put("whoseApplication", "the Home Office's");
        } else {
            fieldValues.put("whoseApplicationHeading", "Your");
            fieldValues.put("whoseApplication", "your");
        }
        if (hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase)) {
            String legalRepReference = asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)
                .orElse(asylumCase.read(LEGAL_REP_REF_NUMBER_PAPER_J, String.class).orElse(""));
            fieldValues.put("legalRepRefPlusTitle", "\nLegal Rep reference: " + legalRepReference);
        }

        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        List<String> appellantAddress = getAppellantAddressInCountryOrOoc(asylumCase);

        for (int i = 0; i < appellantAddress.size(); i++) {
            fieldValues.put("address_line_" + (i + 1), appellantAddress.get(i));
        }
        return fieldValues;
    }
}
