package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.base.Strings;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;

@Service
public class HearingNoticeFieldMapper {

    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");
    private static final DateTimeFormatter DOCUMENT_TIME_FORMAT = DateTimeFormatter.ofPattern("HHmm");
    private final StringProvider stringProvider;

    public HearingNoticeFieldMapper(
        StringProvider stringProvider
    ) {
        this.stringProvider = stringProvider;
    }

    public Map<String, Object> mapFields(AsylumCase asylumCase) {

        final Map<String, Object> fieldValues = new HashMap<>();

        final HearingCentre listedHearingCentre =
            asylumCase
                .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));
        final Optional<YesOrNo> isSubmitRequirementsAvailable = asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE);

        fieldValues.put("hmcts", "[userImage:hmcts.png]");
        fieldValues.put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""));
        fieldValues.put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""));
        fieldValues.put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("hearingDate", formatDateForRendering(asylumCase.read(LIST_CASE_HEARING_DATE, String.class).orElse("")));
        fieldValues.put("hearingTime", formatTimeForRendering(asylumCase.read(LIST_CASE_HEARING_DATE, String.class).orElse("")));

        fieldValues.put(
            "hearingCentreAddress",
            stringProvider.get("hearingCentreAddress", listedHearingCentre.toString()).orElse("")
                .replaceAll(",\\s*", "\n")
        );

        if (isSubmitRequirementsAvailable.isPresent() && isSubmitRequirementsAvailable.get() == YesOrNo.YES) {
            fieldValues.put("vulnerabilities", asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class).orElse("No special adjustments are being made to accommodate vulnerabilities"));
            fieldValues.put("multimedia", asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class).orElse("No multimedia equipment is being provided"));
            fieldValues.put("singleSexCourt", asylumCase.read(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, String.class).orElse("The court will not be single sex"));
            fieldValues.put("inCamera", asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class).orElse("The hearing will be held in public court"));
            fieldValues.put("otherHearingRequest", asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class).orElse("No other adjustments are being made"));
        } else {
            fieldValues.put("vulnerabilities", asylumCase.read(LIST_CASE_REQUIREMENTS_VULNERABILITIES, String.class).orElse("No special adjustments are being made to accommodate vulnerabilities"));
            fieldValues.put("multimedia", asylumCase.read(LIST_CASE_REQUIREMENTS_MULTIMEDIA, String.class).orElse("No multimedia equipment is being provided"));
            fieldValues.put("singleSexCourt", asylumCase.read(LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT, String.class).orElse("The court will not be single sex"));
            fieldValues.put("inCamera", asylumCase.read(LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT, String.class).orElse("The hearing will be held in public court"));
            fieldValues.put("otherHearingRequest", asylumCase.read(LIST_CASE_REQUIREMENTS_OTHER, String.class).orElse("No other adjustments are being made"));
        }
        fieldValues.put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""));

        return fieldValues;
    }

    public String formatDateForRendering(
        String date
    ) {
        if (!Strings.isNullOrEmpty(date)) {
            return LocalDateTime.parse(date).format(DOCUMENT_DATE_FORMAT);
        }

        return "";
    }

    public String formatTimeForRendering(
        String date
    ) {
        if (!Strings.isNullOrEmpty(date)) {
            return LocalDateTime.parse(date).format(DOCUMENT_TIME_FORMAT);
        }

        return "";
    }
}
