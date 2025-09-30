package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.formatDateForRendering;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.formatDateTimeForRendering;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantAddressAsList;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantAddressAsListOoc;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAppellantInUk;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.HearingNoticeUpdatedTemplateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class InternalEditCaseListingLetterTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;

    private final HearingNoticeUpdatedTemplateProvider hearingNoticeUpdatedTemplateProvider;

    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy");
    private static final DateTimeFormatter DOCUMENT_TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");


    public InternalEditCaseListingLetterTemplate(
        @Value("${internalEditCaseListingLetter.templateName}") String templateName,
        CustomerServicesProvider customerServicesProvider, HearingNoticeUpdatedTemplateProvider hearingNoticeUpdatedTemplateProvider) {
        this.templateName = templateName;
        this.customerServicesProvider = customerServicesProvider;
        this.hearingNoticeUpdatedTemplateProvider = hearingNoticeUpdatedTemplateProvider;
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
        final AsylumCase asylumCaseBefore =
            caseDetailsBefore.getCaseData();

        Map<String, Object> fieldValues = hearingNoticeUpdatedTemplateProvider.mapFieldValues(caseDetails, caseDetailsBefore);

        fieldValues.putAll(getAppellantPersonalisation(asylumCase));

        String oldHearingChannel = getHearingChannel(asylumCaseBefore, "Unknown");
        String newHearingChannel = getHearingChannel(asylumCase, "Unknown");

        fieldValues.put("oldHearingChannel", oldHearingChannel);
        fieldValues.put("hearingChannel", newHearingChannel);
        fieldValues.put("oldHearingTime", formatDateTimeForRendering(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class).orElse(""), DOCUMENT_TIME_FORMAT));
        fieldValues.put("oldHearingDate", formatDateTimeForRendering(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class).orElse(""), DOCUMENT_DATE_FORMAT));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForRendering(LocalDate.now().toString(), DOCUMENT_DATE_FORMAT));
        fieldValues.put("hearingDate", formatDateTimeForRendering(asylumCase.read(LIST_CASE_HEARING_DATE, String.class).orElse(""), DOCUMENT_DATE_FORMAT));
        fieldValues.put("hearingTime", formatDateTimeForRendering(asylumCase.read(LIST_CASE_HEARING_DATE, String.class).orElse(""), DOCUMENT_TIME_FORMAT));
        
        List<String> appellantAddress = isAppellantInUk(asylumCase) ?
            getAppellantAddressAsList(asylumCase) :
            getAppellantAddressAsListOoc(asylumCase);

        for (int i = 0; i < appellantAddress.size(); i++) {
            fieldValues.put("address_line_" + (i + 1), appellantAddress.get(i));
        }
        return fieldValues;
    }

    private String getHearingChannel(AsylumCase asylumCase, String defaultValue) {
        Optional<DynamicList> hearingChannelDl = asylumCase.read(HEARING_CHANNEL, DynamicList.class);

        return hearingChannelDl
            .map(dynamicList -> dynamicList.getValue().getLabel())
            .orElse(defaultValue);
    }
}
