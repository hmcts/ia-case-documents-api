package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REF_NUMBER_PAPER_J;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

@Component
public class DetainedLegalRepRemovedIrcPrisonTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;

    public DetainedLegalRepRemovedIrcPrisonTemplate(
            @Value("${detainedLegalRepRemovedIrcPrisonLetter.templateName}") String templateName,
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

        LocalDate dateOfBirth = asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)
                .map(LocalDate::parse)
                .orElseThrow(() -> new IllegalStateException("appellantDateOfBirth is missing"));

        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));
        fieldValues.put("dateOfBirth", formatDateForNotificationAttachmentDocument(dateOfBirth));
        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("firstName", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""));
        fieldValues.put("lastName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""));
        fieldValues.put("onlineReferenceNumber", asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY));
        
        // Try to get legal rep reference number from primary field, fallback to paper J field if null/empty
        Optional<String> primaryRef = asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class);
        Optional<String> legalRepReferenceNumber;
        
        if (primaryRef.isPresent() && primaryRef.get() != null && !primaryRef.get().trim().isEmpty()) {
            // Primary field has a valid non-empty value
            legalRepReferenceNumber = primaryRef;
        } else {
            // Primary field is null, empty, or not present - try fallback
            Optional<String> fallbackRef = asylumCase.read(LEGAL_REP_REF_NUMBER_PAPER_J, String.class);
            if (fallbackRef.isPresent() && fallbackRef.get() != null && !fallbackRef.get().trim().isEmpty()) {
                legalRepReferenceNumber = fallbackRef;
            } else {
                // Both are empty/null - return the primary field as-is to preserve original behavior
                legalRepReferenceNumber = primaryRef;
            }
        }
        
        fieldValues.put("legalRepReferenceNumber", legalRepReferenceNumber);
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));

        return fieldValues;
    }

}
