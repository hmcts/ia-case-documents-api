package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.HearingNoticeFieldMapper;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

@Component
public class HearingNoticeTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final StringProvider stringProvider;

    public HearingNoticeTemplate(
        @Value("${hearingNoticeDocument.templateName}") String templateName,
        StringProvider stringProvider
    ) {
        this.templateName = templateName;
        this.stringProvider = stringProvider;
    }

    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase =
            caseDetails.getCaseData();

        final HearingNoticeFieldMapper fieldMapper =
            new HearingNoticeFieldMapper(stringProvider);

        final Map<String, Object> fieldValues =
            fieldMapper.mapFields(asylumCase);

        fieldValues.put("vulnerabilities", asylumCase.read(LIST_CASE_REQUIREMENTS_VULNERABILITIES, String.class).orElse(""));
        fieldValues.put("multimedia", asylumCase.read(LIST_CASE_REQUIREMENTS_MULTIMEDIA, String.class).orElse(""));
        fieldValues.put("singleSexCourt", asylumCase.read(LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT, String.class).orElse(""));
        fieldValues.put("inCamera", asylumCase.read(LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT, String.class).orElse(""));
        fieldValues.put("otherHearingRequest", asylumCase.read(LIST_CASE_REQUIREMENTS_OTHER, String.class).orElse(""));

        return fieldValues;
    }
}
