package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.util.HashMap;
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
public class InternalChangeHearingCentreLetterTemplate implements DocumentTemplate<AsylumCase> {


    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private final StringProvider stringProvider;

    public InternalChangeHearingCentreLetterTemplate(
        @Value("${internalChangeHearingCentreLetter.templateName}") String templateName,
        CustomerServicesProvider customerServicesProvider,
        StringProvider stringProvider
    ) {
        this.templateName = templateName;
        this.customerServicesProvider = customerServicesProvider;
        this.stringProvider = stringProvider;
    }

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

        final HearingCentre listedHearingCentre =
            asylumCase
                .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));

        final HearingCentre listedHearingCentreBefore =
                asylumCaseBefore
                        .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                        .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre (before) is not present"));

        final String hearingCentreNameBefore =
                stringProvider
                        .get("hearingCentreName", listedHearingCentreBefore.toString())
                        .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre (before) is not present"));

        final Map<String, Object> fieldValues = new HashMap<>(getAppellantPersonalisation(asylumCase));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));
        fieldValues.put("oldHearingCentre", hearingCentreNameBefore);
        fieldValues.put(
            "newHearingCentre",
            stringProvider.get("hearingCentreName", listedHearingCentre.toString()).orElse(""));

        return fieldValues;
    }
}
