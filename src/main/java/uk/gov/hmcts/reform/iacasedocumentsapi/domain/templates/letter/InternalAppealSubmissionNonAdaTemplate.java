package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class InternalAppealSubmissionNonAdaTemplate implements DocumentTemplate<AsylumCase> {

        private final String templateName;
        private final CustomerServicesProvider customerServicesProvider;
        public InternalAppealSubmissionNonAdaTemplate(
                @Value("${internalAppealSubmissionNonAdaDocument.templateName}") String templateName,
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

                fieldValues.put("hmcts", "[userImage:hmcts.png]");
                fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
                fieldValues.put("detainedEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
                fieldValues.put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""));
                fieldValues.put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""));
                fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));
                fieldValues.put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""));
                fieldValues.put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""));
                fieldValues.put("dueDate", dueDatePlusFourWeeks(asylumCase));

                return fieldValues;
        }

        private String dueDatePlusFourWeeks(AsylumCase asylumCase) {
                LocalDate appealSubmissionDate = asylumCase.read(APPEAL_SUBMISSION_DATE, String.class)
                        .map(LocalDate::parse)
                        .orElseThrow(() -> new IllegalStateException("appealSubmissionDate is missing"));

                return  formatDateForNotificationAttachmentDocument(appealSubmissionDate.plusWeeks(4));
        }
}
