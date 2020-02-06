package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;

import com.google.common.collect.ImmutableMap.Builder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;

@Service
public class PersonalisationProvider {

    private static final String HEARING_CENTRE_ADDRESS = "hearingCentreAddress";
    private final String iaCcdFrontendUrl;
    private final HearingDetailsFinder hearingDetailsFinder;
    private final DirectionFinder directionFinder;
    private final DateTimeExtractor dateTimeExtractor;

    public PersonalisationProvider(
        @Value("${iaCcdFrontendUrl}") String iaCcdFrontendUrl,
        HearingDetailsFinder hearingDetailsFinder,
        DirectionFinder directionFinder,
        DateTimeExtractor dateTimeExtractor) {
        this.iaCcdFrontendUrl = iaCcdFrontendUrl;
        this.hearingDetailsFinder = hearingDetailsFinder;
        this.directionFinder = directionFinder;
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

        final Builder<String, String> caseListingValues = ImmutableMap
            .<String, String>builder()
            .put("Hyperlink to user’s case list", iaCcdFrontendUrl)
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("oldHearingCentre", hearingCentreNameBefore)
            .put("oldHearingDate", oldHearingDate == null || oldHearingDate.isEmpty() ? "" : dateTimeExtractor.extractHearingDate(oldHearingDate))
            .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDateTime))
            .put("hearingTime", dateTimeExtractor.extractHearingTime(hearingDateTime))
            .put(HEARING_CENTRE_ADDRESS, hearingCentreAddress);

        buildHearingRequirementsFields(asylumCase, caseListingValues);

        return caseListingValues.build();
    }

    public static void buildHearingRequirementsFields(AsylumCase asylumCase, Builder<String, String> caseListingValues) {

        final Optional<YesOrNo> isSubmitRequirementsAvailable = asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE);

        if (isSubmitRequirementsAvailable.isPresent() && isSubmitRequirementsAvailable.get() == YesOrNo.YES) {

            caseListingValues
                .put("Hearing Requirement Vulnerabilities", readStringCaseField(asylumCase, VULNERABILITIES_TRIBUNAL_RESPONSE,
                    "No special adjustments are being made to accommodate vulnerabilities"))
                .put("Hearing Requirement Multimedia", readStringCaseField(asylumCase, MULTIMEDIA_TRIBUNAL_RESPONSE,
                  "No multimedia equipment is being provided"))
                .put("Hearing Requirement Single Sex Court", readStringCaseField(asylumCase, SINGLE_SEX_COURT_TRIBUNAL_RESPONSE,
                  "The court will not be single sex"))
                .put("Hearing Requirement In Camera Court", readStringCaseField(asylumCase, IN_CAMERA_COURT_TRIBUNAL_RESPONSE,
                  "The hearing will be held in public court"))
                .put("Hearing Requirement Other", readStringCaseField(asylumCase, ADDITIONAL_TRIBUNAL_RESPONSE,
                  "No other adjustments are being made"));
        } else {

            caseListingValues
                .put("Hearing Requirement Vulnerabilities", readStringCaseField(asylumCase, LIST_CASE_REQUIREMENTS_VULNERABILITIES,
                    "No special adjustments are being made to accommodate vulnerabilities"))
                .put("Hearing Requirement Multimedia", readStringCaseField(asylumCase, LIST_CASE_REQUIREMENTS_MULTIMEDIA,
                    "No multimedia equipment is being provided"))
                .put("Hearing Requirement Single Sex Court", readStringCaseField(asylumCase, LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT,
                    "The court will not be single sex"))
                .put("Hearing Requirement In Camera Court", readStringCaseField(asylumCase, LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT,
                    "The hearing will be held in public court"))
                .put("Hearing Requirement Other", readStringCaseField(asylumCase, LIST_CASE_REQUIREMENTS_OTHER,
                    "No other adjustments are being made"));
        }
    }

    public Map<String, String> getNonStandardDirectionPersonalisation(AsylumCase asylumCase) {

        final Direction direction =
            directionFinder
                .findFirst(asylumCase, DirectionTag.NONE)
                .orElseThrow(() -> new IllegalStateException("non-standard direction is not present"));

        final String directionDueDate =
            LocalDate
                .parse(direction.getDateDue())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("iaCaseListHyperLink", iaCcdFrontendUrl)
            .put("explanation", direction.getExplanation())
            .put("dueDate", directionDueDate)
            .build();

    }

    public Map<String, String> getSubmittedHearingRequirementsPersonalisation(AsylumCase asylumCase) {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .build();
    }

    public Map<String, String> getUploadAdditionalEvidencePersonalisation(AsylumCase asylumCase) {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse("N/A"))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .build();
    }

    public Map<String, String> getReviewedHearingRequirementsPersonalisation(AsylumCase asylumCase) {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .build();
    }

    private static String readStringCaseField(final AsylumCase asylumCase, final AsylumCaseDefinition caseField, final String defaultIfNotPresent) {

        final Optional<String> optionalFieldValue = asylumCase.read(caseField, String.class);
        return optionalFieldValue.isPresent() && !optionalFieldValue.get().isEmpty() ? optionalFieldValue.get() : defaultIfNotPresent;
    }
}
