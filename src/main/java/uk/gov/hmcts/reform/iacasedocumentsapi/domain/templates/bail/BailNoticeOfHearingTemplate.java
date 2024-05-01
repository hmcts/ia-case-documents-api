package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DETAINED_LOC;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_PRISON_DETAILS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.IRC_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.IS_REMOTE_HEARING;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_REFERENCE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.LISTING_LOCATION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.PRISON_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.REF_DATA_LISTING_LOCATION_DETAIL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ApplicantDetainedLocation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.model.refdata.CourtVenue;

public class BailNoticeOfHearingTemplate {

    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");
    private static final DateTimeFormatter DOCUMENT_TIME_FORMAT = DateTimeFormatter.ofPattern("HHmm");
    private static final String REMOTE_HEARING_LOCATION = "Cloud Video Platform (CVP)";
    private final CustomerServicesProvider customerServicesProvider;
    private final StringProvider stringProvider;

    public BailNoticeOfHearingTemplate(
        CustomerServicesProvider customerServicesProvider, StringProvider stringProvider) {
        this.customerServicesProvider = customerServicesProvider;
        this.stringProvider = stringProvider;
    }

    public Map<String, Object> mapFieldValues(
        CaseDetails<BailCase> caseDetails
    ) {
        final BailCase bailCase = caseDetails.getCaseData();
        final Map<String, Object> fieldValues = new HashMap<>();
        final String listingHearingDate = bailCase.read(LISTING_HEARING_DATE, String.class).orElse("");

        fieldValues.put("hmcts", "[userImage:hmcts.png]");
        fieldValues.put("applicantGivenNames", bailCase.read(APPLICANT_GIVEN_NAMES, String.class).orElse(""));
        fieldValues.put("applicantFamilyName", bailCase.read(APPLICANT_FAMILY_NAME, String.class).orElse(""));
        fieldValues.put("OnlineCaseReferenceNumber", bailCase.read(BAIL_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("homeOfficeReferenceNumber", bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("legalRepReference", bailCase.read(LEGAL_REP_REFERENCE, String.class).orElse(""));
        fieldValues.put("applicantDetainedLoc", getApplicantDetainedLocation(bailCase));
        fieldValues.put("applicantPrisonDetails", bailCase.read(APPLICANT_PRISON_DETAILS, String.class).orElse(""));
        fieldValues.put("hearingCentreAddress", getListingLocationAddressFromRefDataOrCcd(bailCase));
        fieldValues.put("hearingDate", formatDateForRendering(listingHearingDate));
        fieldValues.put("hearingTime", formatTimeForRendering(listingHearingDate));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getCustomerServicesTelephone());
        fieldValues.put("customerServicesEmail", customerServicesProvider.getCustomerServicesEmail());

        return fieldValues;
    }

    public String getListingLocationAddressFromRefDataOrCcd(BailCase bailCase) {
        String hearingLocationAddress = getListinglocationAddress(bailCase);
        YesOrNo isBailsLocationRefDataEnabled = bailCase.read(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, YesOrNo.class)
                .orElse(NO);

        if (isBailsLocationRefDataEnabled == YES) {
            if (bailCase.read(IS_REMOTE_HEARING, YesOrNo.class).orElse(NO) == YES) {
                return REMOTE_HEARING_LOCATION;
            } else {
                Optional<CourtVenue> refDataListingLocationDetail = bailCase.read(REF_DATA_LISTING_LOCATION_DETAIL, CourtVenue.class);

                if (refDataListingLocationDetail.isPresent()) {
                    hearingLocationAddress = (refDataListingLocationDetail.get().getCourtName() + ", " +
                            refDataListingLocationDetail.get().getCourtAddress() + ", " +
                            refDataListingLocationDetail.get().getPostcode());

                }
            }
        }
        return hearingLocationAddress;
    }

    private String formatDateForRendering(
        String date
    ) {
        if (isNullOrEmptyString(date)) {
            return "";
        }

        return LocalDateTime.parse(date).format(DOCUMENT_DATE_FORMAT);
    }

    private String formatTimeForRendering(String date) {
        if (isNullOrEmptyString(date)) {
            return "";
        }

        return LocalDateTime.parse(date).format(DOCUMENT_TIME_FORMAT);
    }

    private String getListinglocationAddress(BailCase bailCase) {
        String listingLocation = bailCase.read(LISTING_LOCATION, String.class).orElse("");
        if (isNullOrEmptyString(listingLocation)) {
            return "";
        }

        return stringProvider.get("hearingCentreAddress", listingLocation).orElse("")
            .replaceAll(",\\s*", "\n");
    }

    private String getApplicantDetainedLocation(BailCase bailCase) {
        String location = bailCase.read(APPLICANT_DETAINED_LOC, String.class).orElse("");

        String detentionLocation = location.equals(ApplicantDetainedLocation.PRISON.getCode())
            ? bailCase.read(PRISON_NAME, String.class).orElse("") : location.equals(ApplicantDetainedLocation.IMIGRATION_REMOVAL_CENTER.getCode())
            ? bailCase.read(IRC_NAME, String.class).orElse("") : "";

        return detentionLocation;
    }

    private boolean isNullOrEmptyString(String str) {
        return str == null || str.isBlank();
    }
}
