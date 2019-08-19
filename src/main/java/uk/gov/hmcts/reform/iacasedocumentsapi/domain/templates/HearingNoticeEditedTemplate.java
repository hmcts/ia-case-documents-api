package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.HearingNoticeFieldMapper;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

@Component
public class HearingNoticeEditedTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final StringProvider stringProvider;

    public HearingNoticeEditedTemplate(
        @Value("${hearingNoticeEditedDocument.templateName}") String templateName,
        StringProvider stringProvider
    ) {
        this.templateName = templateName;
        this.stringProvider = stringProvider;
    }

    public String getName() {
        return templateName;
    }

    @Override
    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails,
        CaseDetails<AsylumCase> caseDetailsBefore
    ) {
        final AsylumCase asylumCase =
            caseDetails.getCaseData();

        final AsylumCase asylumCaseBefore =
            caseDetailsBefore.getCaseData();

        final HearingNoticeFieldMapper fieldMapper
            = new HearingNoticeFieldMapper(stringProvider);

        final Map<String, Object> fieldValues =
            fieldMapper.mapFields(asylumCase);

        final HearingCentre listedHearingCentreBefore =
            asylumCaseBefore
                .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre (before) is not present"));

        fieldValues.put("oldHearingCentre", fieldMapper.formatHearingCentreForRendering(listedHearingCentreBefore.toString()));
        fieldValues.put("oldHearingDate", fieldMapper.formatDateForRendering(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class).orElse("")));
        fieldValues.put("vulnerabilities", asylumCase.read(LIST_CASE_REQUIREMENTS_VULNERABILITIES, String.class).orElse("No special adjustments are being made to accommodate vulnerabilities"));
        fieldValues.put("multimedia", asylumCase.read(LIST_CASE_REQUIREMENTS_MULTIMEDIA, String.class).orElse("No multimedia equipment is being provided"));
        fieldValues.put("singleSexCourt", asylumCase.read(LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT, String.class).orElse("The court will not be single sex"));
        fieldValues.put("inCamera", asylumCase.read(LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT, String.class).orElse("The hearing will be held in public court"));
        fieldValues.put("otherHearingRequest", asylumCase.read(LIST_CASE_REQUIREMENTS_OTHER, String.class).orElse("No other adjustments are being made"));
        fieldValues.put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""));

        return fieldValues;
    }
}
