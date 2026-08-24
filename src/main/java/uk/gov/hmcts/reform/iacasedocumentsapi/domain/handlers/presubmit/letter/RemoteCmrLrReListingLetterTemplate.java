package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantAddressAsList;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantAddressAsListOoc;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getCmrHearingChannel;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAppellantInUk;

@Component
public class RemoteCmrLrReListingLetterTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private final StringProvider stringProvider;
    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy");
    private static final DateTimeFormatter DOCUMENT_TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    public RemoteCmrLrReListingLetterTemplate(
            @Value("${remoteCmrLrReListingLetter.templateName}") String templateName,
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
            CaseDetails<AsylumCase> caseDetails,
            CaseDetails<AsylumCase> caseDetailsBefore
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();
        final AsylumCase asylumCaseBefore = caseDetailsBefore.getCaseData();

        final HearingCentre listedHearingCentre =
                asylumCase
                        .read(CMR_HEARING_CENTRE, HearingCentre.class)
                        .orElseThrow(() ->
                                new IllegalStateException("listCaseHearingCentre is not present"));

        final HearingCentre oldHearingCentre =
                asylumCaseBefore
                        .read(CMR_HEARING_CENTRE, HearingCentre.class)
                        .orElseThrow(() ->
                                new IllegalStateException("previous listCaseHearingCentre is not present"));

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put(
                "customerServicesTelephone",
                customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)
        );
        fieldValues.put(
                "customerServicesEmail",
                customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)
        );

        fieldValues.put(
                "hearingCentreAddress",
                stringProvider
                        .get("hearingCentreAddress", listedHearingCentre.toString())
                        .orElse("")
                        .replaceAll(",\\s*", "\n")
        );

        fieldValues.put(
                "hearingDate",
                formatDateTimeForRendering(
                        asylumCase.read(CMR_HEARING_DATE, String.class).orElse(""),
                        DOCUMENT_DATE_FORMAT
                )
        );

        fieldValues.put(
                "hearingTime",
                formatDateTimeForRendering(
                        asylumCase.read(CMR_HEARING_DATE, String.class).orElse(""),
                        DOCUMENT_TIME_FORMAT
                )
        );

        fieldValues.put(
                "dateLetterSent",
                formatDateForRendering(
                        LocalDate.now().toString(),
                        DOCUMENT_DATE_FORMAT
                )
        );

        fieldValues.put("hearingChannel", getCmrHearingChannel(asylumCase, "Unknown"));

        // Previous hearing details
        fieldValues.put(
                "oldHearingCentre",
                stringProvider
                        .get("hearingCentreName", oldHearingCentre.toString())
                        .orElse("")
        );

        fieldValues.put(
                "oldHearingDate",
                formatDateTimeForRendering(
                        asylumCaseBefore.read(CMR_HEARING_DATE, String.class).orElse(""),
                        DOCUMENT_DATE_FORMAT
                )
        );

        fieldValues.put(
                "oldHearingTime",
                formatDateTimeForRendering(
                        asylumCaseBefore.read(CMR_HEARING_DATE, String.class).orElse(""),
                        DOCUMENT_TIME_FORMAT
                )
        );

        fieldValues.put(
                "oldHearingChannel",
                getCmrHearingChannel(asylumCaseBefore, "Unknown")
        );

        List<String> appellantAddress = isAppellantInUk(asylumCase)
                ? getAppellantAddressAsList(asylumCase)
                : getAppellantAddressAsListOoc(asylumCase);

        for (int i = 0; i < appellantAddress.size(); i++) {
            fieldValues.put("address_line_" + (i + 1), appellantAddress.get(i));
        }

        return fieldValues;
    }
}
