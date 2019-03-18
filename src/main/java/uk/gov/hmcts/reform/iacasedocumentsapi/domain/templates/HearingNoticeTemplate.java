package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import com.google.common.base.Strings;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

@Component
public class HearingNoticeTemplate implements DocumentTemplate<AsylumCase> {

    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");
    private static final DateTimeFormatter DOCUMENT_TIME_FORMAT = DateTimeFormatter.ofPattern("HHmm");

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
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final HearingCentre listedHearingCentre =
            asylumCase
                .getListCaseHearingCentre()
                .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.put("hmcts", "[userImage:hmcts.png]");

        fieldValues.put("appealReferenceNumber", asylumCase.getAppealReferenceNumber().orElse(""));
        fieldValues.put("appellantGivenNames", asylumCase.getAppellantGivenNames().orElse(""));
        fieldValues.put("appellantFamilyName", asylumCase.getAppellantFamilyName().orElse(""));
        fieldValues.put("homeOfficeReferenceNumber", asylumCase.getHomeOfficeReferenceNumber().orElse(""));
        fieldValues.put("legalRepReferenceNumber", asylumCase.getLegalRepReferenceNumber().orElse(""));

        fieldValues.put("hearingDate", formatDateForRendering(asylumCase.getListCaseHearingDate().orElse("")));
        fieldValues.put("hearingTime", formatTimeForRendering(asylumCase.getListCaseHearingDate().orElse("")));

        fieldValues.put(
            "hearingCentreAddress",
            stringProvider.get("hearingCentreAddress", listedHearingCentre.toString()).orElse("")
                .replaceAll(",\\s*", "\n")
        );

        fieldValues.put("vulnerabilities", asylumCase.getListCaseRequirementsVulnerabilities().orElse(""));
        fieldValues.put("multimedia", asylumCase.getListCaseRequirementsMultimedia().orElse(""));
        fieldValues.put("singleSexCourt", asylumCase.getListCaseRequirementsSingleSexCourt().orElse(""));
        fieldValues.put("inCamera", asylumCase.getListCaseRequirementsInCameraCourt().orElse(""));
        fieldValues.put("otherHearingRequest", asylumCase.getListCaseRequirementsOther().orElse(""));

        return fieldValues;
    }

    private String formatDateForRendering(
        String date
    ) {
        if (!Strings.isNullOrEmpty(date)) {
            return LocalDateTime.parse(date).format(DOCUMENT_DATE_FORMAT);
        }

        return "";
    }

    private String formatTimeForRendering(
        String date
    ) {
        if (!Strings.isNullOrEmpty(date)) {
            return LocalDateTime.parse(date).format(DOCUMENT_TIME_FORMAT);
        }

        return "";
    }
}
