package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_DATE;

import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.HearingNoticeFieldMapper;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

@Component
public class HearingNoticeUpdatedTemplateProvider {

    private final StringProvider stringProvider;

    public HearingNoticeUpdatedTemplateProvider(StringProvider stringProvider) {
        this.stringProvider = stringProvider;
    }

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

        final String hearingCentreNameBefore =
            stringProvider
                .get("hearingCentreName", listedHearingCentreBefore.toString())
                .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre (before) is not present"));

        fieldValues.put("oldHearingCentre", hearingCentreNameBefore);
        fieldValues.put("oldHearingDate", fieldMapper.formatDateForRendering(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class).orElse("")));

        return fieldValues;
    }
}
