package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CMR_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CMR_HEARING_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.formatDateForRendering;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.formatDateTimeForRendering;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getCmrHearingChannel;

/**
 * Common field mapping for the letters generated when a case management review hearing is re-listed.
 * Subclasses only supply the address the letter is sent to.
 */
@Slf4j
public abstract class AbstractInternalCmrReListingLetterTemplate implements DocumentTemplate<AsylumCase> {

    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy");
    private static final DateTimeFormatter DOCUMENT_TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private final StringProvider stringProvider;

    protected AbstractInternalCmrReListingLetterTemplate(
        String templateName,
        CustomerServicesProvider customerServicesProvider,
        StringProvider stringProvider
    ) {
        this.templateName = templateName;
        this.customerServicesProvider = customerServicesProvider;
        this.stringProvider = stringProvider;
    }

    @Override
    public String getName() {
        return templateName;
    }

    @Override
    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails,
        CaseDetails<AsylumCase> caseDetailsBefore
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();
        final AsylumCase asylumCaseBefore = caseDetailsBefore.getCaseData();

        final HearingCentre hearingCentre =
            asylumCase
                .read(CMR_HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("cmrHearingCentre is not present"));

        final HearingCentre hearingCentreBefore =
            asylumCaseBefore
                .read(CMR_HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("cmrHearingCentre (before) is not present"));

        final String hearingCentreNameBefore =
            stringProvider
                .get("hearingCentreName", hearingCentreBefore.toString())
                .orElseThrow(() -> new IllegalStateException("cmrHearingCentre (before) is not present"));

        final Map<String, Object> fieldValues = new HashMap<>(getPersonalisation(asylumCase));

        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForRendering(LocalDate.now().toString(), DOCUMENT_DATE_FORMAT));

        log.info("----------asylumCase.read(CMR_HEARING_DATE, String.class): " + asylumCase.read(CMR_HEARING_DATE, String.class));
        log.info("----------asylumCaseBefore.read(CMR_HEARING_DATE, String.class): " + asylumCaseBefore.read(CMR_HEARING_DATE, String.class));
        fieldValues.put("hearingCentreAddress", stringProvider.get("hearingCentreAddress", hearingCentre.toString()).orElse("").replaceAll(",\\s*", "\n"));
        fieldValues.put("hearingDate", formatDateTimeForRendering(asylumCase.read(CMR_HEARING_DATE, String.class).orElse(""), DOCUMENT_DATE_FORMAT));
        fieldValues.put("hearingTime", formatDateTimeForRendering(asylumCase.read(CMR_HEARING_DATE, String.class).orElse(""), DOCUMENT_TIME_FORMAT));
        fieldValues.put("hearingChannel", getCmrHearingChannel(asylumCase, "Unknown"));

        fieldValues.put("oldHearingCentre", hearingCentreNameBefore);
        fieldValues.put("oldHearingDate", formatDateTimeForRendering(asylumCaseBefore.read(CMR_HEARING_DATE, String.class).orElse(""), DOCUMENT_DATE_FORMAT));
        fieldValues.put("oldHearingTime", formatDateTimeForRendering(asylumCaseBefore.read(CMR_HEARING_DATE, String.class).orElse(""), DOCUMENT_TIME_FORMAT));
        fieldValues.put("oldHearingChannel", getCmrHearingChannel(asylumCaseBefore, "Unknown"));

        List<String> recipientAddress = getRecipientAddress(asylumCase);

        for (int i = 0; i < recipientAddress.size(); i++) {
            fieldValues.put("address_line_" + (i + 1), recipientAddress.get(i));
        }
        return fieldValues;
    }

    protected abstract List<String> getRecipientAddress(AsylumCase asylumCase);

    protected Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return getAppellantPersonalisation(asylumCase);
    }
}
