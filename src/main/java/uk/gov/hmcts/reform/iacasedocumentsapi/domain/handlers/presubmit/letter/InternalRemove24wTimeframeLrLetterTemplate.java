package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getLegalRepAddressInCountryOrOoc;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getLegalRepPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.setCompleteCaseReviewContentForRemoval;

@Component
public class InternalRemove24wTimeframeLrLetterTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;

    public InternalRemove24wTimeframeLrLetterTemplate(
        @Value("${internalRemove24wTimeframeLetter.templateName}") String templateName,
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

        final Map<String, Object> fieldValues = new HashMap<>(getLegalRepPersonalisation(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));

        fieldValues.put("appellantNameField", "\nAppellant name: " +
            asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse("")
            + " " + asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""));
        fieldValues.put("legalRepRefPlusTitle", "\nYour reference: "
            + asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""));

        setCompleteCaseReviewContentForRemoval(asylumCase, fieldValues);

        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        List<String> appellantAddress = getLegalRepAddressInCountryOrOoc(asylumCase);

        for (int i = 0; i < appellantAddress.size(); i++) {
            fieldValues.put("address_line_" + (i + 1), appellantAddress.get(i));
        }
        return fieldValues;
    }
}
