package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
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
public class InternalAdaSuitabilityReviewUnsuitableLetterTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;

    public InternalAdaSuitabilityReviewUnsuitableLetterTemplate(
        @Value("${adaInternalSuitabilityReviewUnsuitableDocument.templateName}") String templateName,
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
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("ADAemail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));
        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("hearingType", asylumCase.read(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class).orElse(null));
        fieldValues.put("vulnerabilities", asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class).orElse(null));
        fieldValues.put("pastExperiences", asylumCase.read(PAST_EXPERIENCES_TRIBUNAL_RESPONSE, String.class).orElse(null));
        fieldValues.put("multimedia", asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class).orElse(null));
        fieldValues.put("singleSexCourt", asylumCase.read(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, String.class).orElse(null));
        fieldValues.put("inCamera", asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class).orElse(null));
        fieldValues.put("otherHearingRequest", asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class).orElse(null));

        return fieldValues;
    }
}
