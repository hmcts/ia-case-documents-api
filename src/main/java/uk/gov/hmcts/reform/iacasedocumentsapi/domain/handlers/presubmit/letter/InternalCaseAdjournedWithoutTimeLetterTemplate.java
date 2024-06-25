package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.ADJOURN_HEARING_WITHOUT_DATE_REASONS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.formatDateForRendering;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.formatDateTimeForRendering;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantAddressAsList;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class InternalCaseAdjournedWithoutTimeLetterTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private final StringProvider stringProvider;
    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy");

    public InternalCaseAdjournedWithoutTimeLetterTemplate(
        @Value("${internalAdjournWithoutDateLetter.templateName}") String templateName,
        CustomerServicesProvider customerServicesProvider,
        StringProvider stringProvider) {
        this.templateName = templateName;
        this.customerServicesProvider = customerServicesProvider;
        this.stringProvider = stringProvider;
    }

    @Override
    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();
        final HearingCentre listedHearingCentre =
            asylumCase
                .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("hearingLocation", stringProvider.get("hearingCentreAddress", listedHearingCentre.toString()).orElse("").replaceAll(",\\s*", "\n"));
        fieldValues.put("hearingDate", formatDateTimeForRendering(asylumCase.read(LIST_CASE_HEARING_DATE, String.class).orElse(""), DOCUMENT_DATE_FORMAT));
        fieldValues.put("dateLetterSent", formatDateForRendering(LocalDate.now().toString(), DOCUMENT_DATE_FORMAT));
        fieldValues.put("adjournedHearingReason", asylumCase.read(ADJOURN_HEARING_WITHOUT_DATE_REASONS, String.class).orElse(""));

        List<String> appellantAddress = getAppellantAddressAsList(asylumCase);

        for (int i = 0; i < appellantAddress.size(); i++) {
            fieldValues.put("address_line_" + (i + 1), appellantAddress.get(i));
        }
        return fieldValues;
    }
}
