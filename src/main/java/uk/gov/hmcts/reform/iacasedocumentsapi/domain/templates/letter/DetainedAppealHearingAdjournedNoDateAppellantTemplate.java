package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

@Component
public class DetainedAppealHearingAdjournedNoDateAppellantTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;

    public DetainedAppealHearingAdjournedNoDateAppellantTemplate(
            @Value("${detainedHearingAdjournedNoDateAppellantLetter.templateName}") String templateName,
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
    public Map<String, Object> mapFieldValues(CaseDetails<AsylumCase> caseDetails, CaseDetails<AsylumCase> caseDetailsBefore) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final Map<String, Object> fieldValues = new HashMap<>();

        String previousHearingCentre;
        Optional<String> hearingCentre = caseDetailsBefore.getCaseData().read(LIST_CASE_HEARING_CENTRE, String.class);
        if (hearingCentre.isPresent()) {
            previousHearingCentre = hearingCentre.get();
        } else {
            previousHearingCentre = "";
        }

        String previousHearingDate;
        Optional<String> listCaseHearingDate = caseDetailsBefore.getCaseData().read(LIST_CASE_HEARING_DATE, String.class);
        if (listCaseHearingDate.isPresent()) {
            LocalDateTime hearingDateTime = LocalDateTime.parse(listCaseHearingDate.get());
            previousHearingDate = formatDateForNotificationAttachmentDocument(hearingDateTime.toLocalDate());
        } else {
            previousHearingDate = "";
        }

        String reasonForAdjournedHearing = asylumCase.read(ADJOURN_HEARING_WITHOUT_DATE_REASONS, String.class).orElse("");

        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));
        fieldValues.put("oldHearingCentre", previousHearingCentre);
        fieldValues.put("oldHearingDate", previousHearingDate);
        fieldValues.put("reasonForAdjournedHearing", reasonForAdjournedHearing);
        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("onlineCaseRefNumber", asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY).orElse(""));
        fieldValues.put("oldHearingCentre", previousHearingCentre);
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));

        return fieldValues;
    }
}
