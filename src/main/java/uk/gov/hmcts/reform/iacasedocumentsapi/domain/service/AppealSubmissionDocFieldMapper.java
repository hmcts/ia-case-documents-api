package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static java.util.stream.Collectors.joining;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.OutOfCountryCircumstances.ENTRY_CLEARANCE_DECISION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.OutOfCountryCircumstances.LEAVE_UK;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.formatDateForRendering;

import com.google.common.collect.ImmutableMap;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.OutOfCountryCircumstances;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.OutOfCountryDecisionType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.*;

@Service
public class AppealSubmissionDocFieldMapper {

    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");
    public static final String CIRCUMSTANCES_OF_THE_APPELLANT_S_OUT_OF_COUNTRY_APPEAL_TITLE = "Circumstances of the appellant's out of country appeal";
    public static final String THE_APPELLANT_IS_APPEALING_AN_ENTRY_CLEARANCE_DECISION = "The appellant is appealing an entry clearance decision";
    public static final String THE_APPELLANT_HAD_TO_LEAVE_THE_UK_IN_ORDER_TO_APPEAL = "The appellant had to leave the UK in order to appeal";
    public static final String OUT_OF_COUNTRY_DECISION_TYPE_TITLE = "outOfCountryDecisionTypeTitle";
    public static final String OUT_OF_COUNTRY_DECISION_TYPE_TEXT = "outOfCountryDecisionType";
    private final StringProvider stringProvider;

    public AppealSubmissionDocFieldMapper(
        StringProvider stringProvider
    ) {
        this.stringProvider = stringProvider;
    }

    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("CREATED_DATE", caseDetails.getCreatedDate().format(DOCUMENT_DATE_FORMAT));
        fieldValues.put("appealSubmissionDate", formatDateForRendering(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class).orElse(""), DOCUMENT_DATE_FORMAT));
        fieldValues.put("legalRepresentativeEmailAddress", asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class).orElse(""));
        fieldValues.put("legalRepName", formatLegalRepName(
            asylumCase.read(LEGAL_REP_NAME, String.class).orElse(""),
            asylumCase.read(LEGAL_REP_FAMILY_NAME, String.class).orElse("")));
        fieldValues.put("legalRepCompany", asylumCase.read(LEGAL_REP_COMPANY, String.class).orElse(""));
        fieldValues.put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("appellantDateOfBirth", formatDateForRendering(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class).orElse(""), DOCUMENT_DATE_FORMAT));
        fieldValues.put("appellantTitle", asylumCase.read(APPELLANT_TITLE, String.class).orElse(""));

        Optional<String> homeOfficeDecisionDate = asylumCase.read(HOME_OFFICE_DECISION_DATE, String.class);
        fieldValues.put("homeOfficeDecisionDate", homeOfficeDecisionDate.isPresent()
            ? formatDateForRendering(asylumCase.read(HOME_OFFICE_DECISION_DATE, String.class).orElse(""), DOCUMENT_DATE_FORMAT) : null);

        Optional<String> decisionLetterReceivedDate = asylumCase.read(DECISION_LETTER_RECEIVED_DATE, String.class);
        fieldValues.put("decisionLetterReceivedDate", decisionLetterReceivedDate.isPresent()
            ? formatDateForRendering(asylumCase.read(DECISION_LETTER_RECEIVED_DATE, String.class).orElse(""), DOCUMENT_DATE_FORMAT) : null);

        Optional<YesOrNo> isDetained = Optional.of(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class).orElse(YesOrNo.NO));
        if (isDetained.equals(Optional.of(YesOrNo.YES))) {
            populateDetainedFields(asylumCase, fieldValues);
        }
        fieldValues.put("appellantInDetention", isDetained);

        Optional<ContactPreference> contactPreference = asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class);
        if (contactPreference.isPresent()
            && contactPreference.get().toString().equals(ContactPreference.WANTS_EMAIL.toString())) {
            fieldValues.put("wantsEmail", YesOrNo.YES);
            fieldValues.put("email", asylumCase.read(EMAIL, String.class).orElse(""));
        } else {
            fieldValues.put("mobileNumber", asylumCase.read(MOBILE_NUMBER, String.class).orElse(""));
        }

        fieldValues.put("hasSponsor", YesOrNo.NO);
        fieldValues.put("appealOutOfCountry", asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class).orElse(YesOrNo.NO));

        addAppealOocFields(asylumCase, fieldValues);

        Optional<AsylumAppealType> optionalAppealType = asylumCase.read(APPEAL_TYPE, AsylumAppealType.class);

        if (optionalAppealType.isPresent()) {
            String appealType = optionalAppealType.get().getValue();
            fieldValues.put(
                "appealType",
                stringProvider.get("appealType", appealType).orElse("")
            );
        }

        YesOrNo removalOrderOption = asylumCase.read(REMOVAL_ORDER_OPTIONS, YesOrNo.class).orElse(YesOrNo.NO);
        if (removalOrderOption.equals(YesOrNo.YES)) {
            String removalOrderDate = asylumCase.read(REMOVAL_ORDER_DATE, String.class).orElse("");
            if (removalOrderDate.isBlank()) {
                removalOrderOption = YesOrNo.NO;
            } else {
                fieldValues.put("removalOrderDate", removalOrderDate);
            }
        }
        fieldValues.put("removalOrderOption", removalOrderOption);

        fieldValues.put("newMatters", asylumCase.read(NEW_MATTERS, String.class).orElse(""));

        if (asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            populateAddressFields(asylumCase, fieldValues);
        }

        Optional<List<IdValue<Map<String, String>>>> appellantNationalities = asylumCase
            .read(APPELLANT_NATIONALITIES);

        fieldValues.put(
            "appellantNationalities",
            appellantNationalities
                .orElse(Collections.emptyList())
                .stream()
                .filter(idValue -> idValue.getValue().containsKey("code"))
                .map(idValue -> idValue.getValue().get("code"))
                .map(code -> stringProvider.get("isoCountries", code))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(nationality -> ImmutableMap.of("nationality", nationality))
                .collect(Collectors.toList())
        );

        Optional<List<String>> groundsOfAppealForDisplay = asylumCase
            .read(APPEAL_GROUNDS_FOR_DISPLAY);

        fieldValues.put(
            "appealGrounds",
            groundsOfAppealForDisplay
                .orElse(Collections.emptyList())
                .stream()
                .map(code -> stringProvider.get("appealGrounds", code))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(appealGround -> ImmutableMap.of("appealGround", appealGround))
                .collect(Collectors.toList())
        );

        Optional<List<IdValue<Map<String, String>>>> otherAppeals = asylumCase
            .read(OTHER_APPEALS);

        populateOtherAppeals(fieldValues, otherAppeals, asylumCase);

        fieldValues.put("applicationOutOfTimeExplanation", asylumCase.read(APPLICATION_OUT_OF_TIME_EXPLANATION, String.class).orElse(""));
        fieldValues.put("submissionOutOfTime", asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class).orElse(YesOrNo.NO));
        fieldValues.put(
            "applicationOutOfTimeDocumentName",
            asylumCase.read(APPLICATION_OUT_OF_TIME_DOCUMENT, Document.class)
                .map(Document::getDocumentFilename)
                .orElse("")
        );

        fieldValues.put("isAdmin", asylumCase.read(IS_ADMIN, YesOrNo.class).orElse(YesOrNo.NO));

        return fieldValues;
    }

    private static void populateOtherAppeals(Map<String, Object> fieldValues, Optional<List<IdValue<Map<String, String>>>> otherAppeals, AsylumCase asylumCase) {
        fieldValues.put(
            "otherAppeals",
            otherAppeals
                .orElse(Collections.emptyList())
                .stream()
                .filter(idValue -> idValue.getValue().containsKey("value"))
                .map(idValue -> idValue.getValue().get("value"))
                .collect(joining(", "))
        );

        Optional<HasOtherAppeals> hasOtherAppeals = asylumCase.read(HAS_OTHER_APPEALS, HasOtherAppeals.class);
        if (hasOtherAppeals.isPresent() && hasOtherAppeals.get().toString().equals(HasOtherAppeals.YES.toString())) {
            fieldValues.put("hasOtherAppeals", YesOrNo.YES);
        } else {
            fieldValues.put("hasOtherAppeals", YesOrNo.NO);
        }
    }

    private static void addAppealOocFields(AsylumCase asylumCase, Map<String, Object> fieldValues) {
        if (asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {

            Optional<OutOfCountryDecisionType> maybeOutOfCountryDecisionType = asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class);
            Optional<OutOfCountryCircumstances> maybeOutOfCountryCircumstances = asylumCase.read(OOC_APPEAL_ADMIN_J, OutOfCountryCircumstances.class);

            if (maybeOutOfCountryCircumstances.isPresent() && isInternalCase(asylumCase) && !isAppellantInUk(asylumCase)) {
                populateOutOfCircumstancesInternalCase(fieldValues, maybeOutOfCountryCircumstances.get());
            } else if (maybeOutOfCountryDecisionType.isPresent()) {
                fieldValues.put(OUT_OF_COUNTRY_DECISION_TYPE_TITLE, "Out of country decision type");

                OutOfCountryDecisionType decisionType = maybeOutOfCountryDecisionType.get();
                fieldValues.put(OUT_OF_COUNTRY_DECISION_TYPE_TEXT, maybeOutOfCountryDecisionType.get().getDescription());
                fieldValues.put("decisionLetterReceived", YesOrNo.YES);

                if (decisionType == OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS || decisionType == OutOfCountryDecisionType.REFUSE_PERMIT) {
                    fieldValues.put("gwfReferenceNumber", asylumCase.read(GWF_REFERENCE_NUMBER, String.class).orElse(null));
                    fieldValues.put("dateEntryClearanceDecision", formatDateForRendering(asylumCase.read(DATE_ENTRY_CLEARANCE_DECISION, String.class).orElse(null), DOCUMENT_DATE_FORMAT));
                    fieldValues.put("decisionLetterReceived", YesOrNo.NO);

                } else if (decisionType == OutOfCountryDecisionType.REFUSAL_OF_PROTECTION) {
                    fieldValues.put("dateClientLeaveUk", formatDateForRendering(asylumCase.read(DATE_CLIENT_LEAVE_UK, String.class).orElse(null), DOCUMENT_DATE_FORMAT));
                    fieldValues.put("didClientLeaveUk", YesOrNo.YES);
                }
            }

            if (asylumCase.read(HAS_CORRESPONDENCE_ADDRESS, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
                fieldValues.put("appellantOutOfCountryAddress", asylumCase.read(APPELLANT_OUT_OF_COUNTRY_ADDRESS, String.class).orElse(""));
            }

            populateSponsorFields(asylumCase, fieldValues);
        }
    }

    private static void populateOutOfCircumstancesInternalCase(Map<String, Object> fieldValues, OutOfCountryCircumstances outOfCountryCircumstances) {
        if (outOfCountryCircumstances.equals(ENTRY_CLEARANCE_DECISION)) {
            fieldValues.put(OUT_OF_COUNTRY_DECISION_TYPE_TITLE, CIRCUMSTANCES_OF_THE_APPELLANT_S_OUT_OF_COUNTRY_APPEAL_TITLE);
            fieldValues.put(OUT_OF_COUNTRY_DECISION_TYPE_TEXT, THE_APPELLANT_IS_APPEALING_AN_ENTRY_CLEARANCE_DECISION);
        } else if (outOfCountryCircumstances.equals(LEAVE_UK)) {
            fieldValues.put(OUT_OF_COUNTRY_DECISION_TYPE_TITLE, CIRCUMSTANCES_OF_THE_APPELLANT_S_OUT_OF_COUNTRY_APPEAL_TITLE);
            fieldValues.put(OUT_OF_COUNTRY_DECISION_TYPE_TEXT, THE_APPELLANT_HAD_TO_LEAVE_THE_UK_IN_ORDER_TO_APPEAL);
        }
    }

    private static void populateSponsorFields(AsylumCase asylumCase, Map<String, Object> fieldValues) {
        Optional<YesOrNo> hasSponsor = asylumCase.read(HAS_SPONSOR, YesOrNo.class);
        if (hasSponsor.isPresent() && hasSponsor.get().equals(YesOrNo.YES)) {
            fieldValues.put("hasSponsor", YesOrNo.YES);
            fieldValues.put("sponsorGivenNames", asylumCase.read(SPONSOR_GIVEN_NAMES, String.class).orElse(null));
            fieldValues.put("sponsorFamilyName", asylumCase.read(SPONSOR_FAMILY_NAME, String.class).orElse(null));
            fieldValues.put("sponsorAddress", asylumCase.read(SPONSOR_ADDRESS_FOR_DISPLAY, String.class).orElse(null));
            Optional<ContactPreference> sponsorContactPreference = asylumCase.read(SPONSOR_CONTACT_PREFERENCE, ContactPreference.class);
            if (sponsorContactPreference.isPresent()
                && sponsorContactPreference.get().toString().equals(ContactPreference.WANTS_EMAIL.toString())) {
                fieldValues.put("wantsSponsorEmail", YesOrNo.YES);
                fieldValues.put("sponsorEmail", asylumCase.read(SPONSOR_EMAIL, String.class).orElse(null));
            } else {
                fieldValues.put("sponsorMobileNumber", asylumCase.read(SPONSOR_MOBILE_NUMBER, String.class).orElse(null));
            }
        }
    }

    private void populateDetainedFields(AsylumCase asylumCase, Map<String, Object> fieldValues) {
        StringBuilder sb = new StringBuilder("Detained");
        YesOrNo isAcceleratedDetainedAppeal = asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class).orElse(YesOrNo.NO);
        if (isAcceleratedDetainedAppeal.equals(YesOrNo.YES)) {
            sb.append(" - Accelerated");
        }
        fieldValues.put("isAcceleratedDetainedAppeal", isAcceleratedDetainedAppeal);
        fieldValues.put("detentionStatus", sb.toString());
        sb.setLength(0);

        String detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class).orElse("");
        String detentionFacilityName = "";
        switch (detentionFacility) {
            case "immigrationRemovalCentre":
                detentionFacility = "Immigration Removal Centre";
                detentionFacilityName = asylumCase.read(IRC_NAME, String.class).orElse("");
                break;
            case "prison":
                detentionFacility = "Prison";
                detentionFacilityName = asylumCase.read(PRISON_NAME, String.class).orElse("");

                YesOrNo nomsAvailable = YesOrNo.NO;
                String nomsNumber = formatComplexString(asylumCase
                    .get("prisonNOMSNumber")
                    .toString());
                if (!nomsNumber.isBlank()) {
                    nomsAvailable = YesOrNo.YES;
                    fieldValues.put("nomsNumber", nomsNumber);
                }
                fieldValues.put("nomsAvailable", nomsAvailable);

                break;
            case "other":
                detentionFacility = "Other";
                detentionFacilityName = formatComplexString(asylumCase.get("otherDetentionFacilityName").toString());
                break;
            default:
                // Required for sonar scan. Can never reach here.
        }

        YesOrNo releaseDateProvided = YesOrNo.NO;
        if (asylumCase.containsKey("dateCustodialSentence")) {
            String prisonerReleaseDate = formatComplexString(asylumCase
                    .get("dateCustodialSentence")
                    .toString());
            if (!prisonerReleaseDate.isBlank()) {
                releaseDateProvided = YesOrNo.YES;
                fieldValues.put("releaseDate", prisonerReleaseDate);
            }
        }
        fieldValues.put("releaseDateProvided", releaseDateProvided);

        fieldValues.put("detentionFacility", detentionFacility);
        fieldValues.put("detentionFacilityName", detentionFacilityName);

        BailApplicationStatus hasBailApplication = asylumCase.read(HAS_PENDING_BAIL_APPLICATIONS, BailApplicationStatus.class).orElse(BailApplicationStatus.NO);
        if (hasBailApplication.equals(BailApplicationStatus.YES)) {
            fieldValues.put("bailApplicationNumber", asylumCase.read(BAIL_APPLICATION_NUMBER, String.class));
        }
        fieldValues.put("hasPendingBailApplication", hasBailApplication);
    }

    private String formatLegalRepName(@NonNull String firstName, @NonNull String lastName) {
        if (!(lastName.isEmpty() || firstName.isEmpty())) {
            return firstName + " " + lastName;
        }
        return firstName;
    }

    private void populateAddressFields(AsylumCase asylumCase, Map<String, Object> fieldValues) {
        Optional<AddressUk> optionalAppellantAddress = asylumCase.read(APPELLANT_ADDRESS);

        if (optionalAppellantAddress.isPresent()) {

            AddressUk appellantAddress = optionalAppellantAddress.get();

            fieldValues.put(
                "appellantAddress",
                ImmutableMap
                    .builder()
                    .put("appellantAddressLine1", appellantAddress.getAddressLine1().orElse(""))
                    .put("appellantAddressLine2", appellantAddress.getAddressLine2().orElse(""))
                    .put("appellantAddressLine3", appellantAddress.getAddressLine3().orElse(""))
                    .put("appellantAddressPostTown", appellantAddress.getPostTown().orElse(""))
                    .put("appellantAddressCounty", appellantAddress.getCounty().orElse(""))
                    .put("appellantAddressPostCode", appellantAddress.getPostCode().orElse(""))
                    .put("appellantAddressCountry", appellantAddress.getCountry().orElse(""))
                    .build()
            );
        }
    }

    public static String formatComplexString(String data) {
        data = data.replaceAll("[\\[\\](){}]", "");
        return data.substring(data.lastIndexOf("=") + 1);
    }

}
