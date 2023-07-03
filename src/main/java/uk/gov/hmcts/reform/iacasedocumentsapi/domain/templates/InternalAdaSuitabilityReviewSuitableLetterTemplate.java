package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class InternalAdaSuitabilityReviewSuitableLetterTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");


    public InternalAdaSuitabilityReviewSuitableLetterTemplate(
        @Value("${adaInternalSuitabilityReviewSuitableDocument.templateName}") String templateName,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.templateName = templateName;
        this.customerServicesProvider = customerServicesProvider;
    }

    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.put("hmcts", "[userImage:hmcts.png]");
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalAdaCustomerServicesTelephone());
        fieldValues.put("ADAemail", customerServicesProvider.getInternalAdaCustomerServicesEmail());
        fieldValues.put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));

        fieldValues.put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""));
        fieldValues.put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""));

        fieldValues.put("hearingDate", formatDateForRendering(asylumCase.read(LIST_CASE_HEARING_DATE, String.class).orElse("")));
        fieldValues.put("hearingType", asylumCase.read(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class).orElse(null));
        fieldValues.put("vulnerabilities", asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class).orElse(null));
        fieldValues.put("pastExperiences", asylumCase.read(PAST_EXPERIENCES_TRIBUNAL_RESPONSE, String.class).orElse(null));
        fieldValues.put("multimedia", asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class).orElse(null));
        fieldValues.put("singleSexCourt", asylumCase.read(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, String.class).orElse(null));
        fieldValues.put("inCamera", asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class).orElse(null));
        fieldValues.put("otherHearingRequest", asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class).orElse(null));

        return fieldValues;
    }

    public String formatDateForRendering(
            String date
    ) {
        if (!Strings.isNullOrEmpty(date)) {
            return LocalDateTime.parse(date).format(DOCUMENT_DATE_FORMAT);
        }

        return "";
    }
}
