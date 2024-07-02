package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.ADJOURN_HEARING_WITHOUT_DATE_REASONS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.formatDateForRendering;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.formatDateTimeForRendering;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantAddressAsList;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantAddressAsListOoc;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAppellantInUk;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;

@Component
public class InternalCaseAdjournedWithoutTimeLetterTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private final HearingDetailsFinder hearingDetailsFinder;
    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy");

    public InternalCaseAdjournedWithoutTimeLetterTemplate(
        @Value("${internalAdjournWithoutDateLetter.templateName}") String templateName,
        CustomerServicesProvider customerServicesProvider,
        HearingDetailsFinder hearingDetailsFinder) {
        this.templateName = templateName;
        this.customerServicesProvider = customerServicesProvider;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    @Override
    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();
        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForRendering(LocalDate.now().toString(), DOCUMENT_DATE_FORMAT));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("hearingLocation", hearingDetailsFinder.getHearingCentreName(asylumCase));
        fieldValues.put("adjournedHearingReason", asylumCase.read(ADJOURN_HEARING_WITHOUT_DATE_REASONS, String.class).orElse(""));
        fieldValues.put("hearingDate", formatDateTimeForRendering(asylumCase.read(LIST_CASE_HEARING_DATE, String.class).orElse(""), DOCUMENT_DATE_FORMAT));

        List<String> appellantAddress = isAppellantInUk(asylumCase) ?
            getAppellantAddressAsList(asylumCase) :
            getAppellantAddressAsListOoc(asylumCase);

        for (int i = 0; i < appellantAddress.size(); i++) {
            fieldValues.put("address_line_" + (i + 1), appellantAddress.get(i));
        }
        return fieldValues;
    }
}
