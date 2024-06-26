package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;

@Component
public class InternalDetGenerateHearingBundleTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private final HearingDetailsFinder hearingDetailsFinder;

    public InternalDetGenerateHearingBundleTemplate(
            @Value("${internalAdaHearingBundle.templateName}") String templateName,
            CustomerServicesProvider customerServicesProvider,
            HearingDetailsFinder hearingDetailsFinder
    ) {
        this.templateName = templateName;
        this.customerServicesProvider = customerServicesProvider;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
            CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));

        final String listCaseHearingDate = asylumCase.read(LIST_CASE_HEARING_DATE, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException("List case hearing date not found."));
        LocalDateTime hearingDateTime = LocalDateTime.parse(listCaseHearingDate);
        fieldValues.put("hearingDate", formatDateForNotificationAttachmentDocument(hearingDateTime.toLocalDate()));
        fieldValues.put("hearingTime", hearingDateTime.toLocalTime());
        fieldValues.put("hearingLocation", hearingDetailsFinder.getHearingCentreName(asylumCase));

        return fieldValues;
    }
}
