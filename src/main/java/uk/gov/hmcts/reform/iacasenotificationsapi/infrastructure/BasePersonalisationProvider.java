package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;

@Service
public class BasePersonalisationProvider {

    private static final String HEARING_CENTRE_ADDRESS = "hearingCentreAddress";
    private final String iaCcdFrontendUrl;
    private final HearingDetailsFinder hearingDetailsFinder;
    private final DateTimeExtractor dateTimeExtractor;

    public BasePersonalisationProvider(
        @Value("${iaCcdFrontendUrl}") String iaCcdFrontendUrl,
        HearingDetailsFinder hearingDetailsFinder,
        DateTimeExtractor dateTimeExtractor) {
        this.iaCcdFrontendUrl = iaCcdFrontendUrl;
        this.hearingDetailsFinder = hearingDetailsFinder;
        this.dateTimeExtractor = dateTimeExtractor;
    }

    public Map<String, String> getEditCaseListingPersonalisation(Callback<AsylumCase> callback) {

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Optional<CaseDetails<AsylumCase>> caseDetailsBefore = callback.getCaseDetailsBefore();

        final String hearingCentreAddress =
            hearingDetailsFinder.getHearingCentreAddress(asylumCase);

        final String hearingDateTime =
            hearingDetailsFinder.getHearingDateTime(asylumCase);

        String hearingCentreNameBefore = "";
        String oldHearingDate = "";

        if (caseDetailsBefore.isPresent()) {

            hearingCentreNameBefore =
                hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.get().getCaseData());

            oldHearingDate =
                hearingDetailsFinder.getHearingDateTime(caseDetailsBefore.get().getCaseData());
        }

        return ImmutableMap
            .<String, String>builder()
            .put("Hyperlink to user’s case list", iaCcdFrontendUrl)
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("Hearing Requirement Vulnerabilities", readStringCaseField(asylumCase, LIST_CASE_REQUIREMENTS_VULNERABILITIES,
                "No special adjustments are being made to accommodate vulnerabilities"))
            .put("Hearing Requirement Multimedia", readStringCaseField(asylumCase, LIST_CASE_REQUIREMENTS_MULTIMEDIA,
                "No multimedia equipment is being provided"))
            .put("Hearing Requirement Single Sex Court", readStringCaseField(asylumCase, LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT,
                "The court will not be single sex"))
            .put("Hearing Requirement In Camera Court", readStringCaseField(asylumCase, LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT,
                "The hearing will be held in public court"))
            .put("Hearing Requirement Other", readStringCaseField(asylumCase, LIST_CASE_REQUIREMENTS_OTHER,
                "No other adjustments are being made"))
            .put("oldHearingCentre", hearingCentreNameBefore)
            .put("oldHearingDate", oldHearingDate == null || oldHearingDate.isEmpty() ? "" : dateTimeExtractor.extractHearingDate(oldHearingDate))
            .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDateTime))
            .put("hearingTime", dateTimeExtractor.extractHearingTime(hearingDateTime))
            .put(HEARING_CENTRE_ADDRESS, hearingCentreAddress)
            .build();
    }

    private String readStringCaseField(final AsylumCase asylumCase, final AsylumCaseDefinition caseField, final String defaultIfNotPresent) {

        final Optional<String> optionalFieldValue = asylumCase.read(caseField, String.class);
        return optionalFieldValue.isPresent() && !optionalFieldValue.get().isEmpty() ? optionalFieldValue.get() : defaultIfNotPresent;
    }
}
