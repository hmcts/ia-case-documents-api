package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static java.util.stream.Collectors.joining;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.OutOfCountryDecisionType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.BailApplicationStatus;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

@Component
public class AppealSubmissionTemplate implements DocumentTemplate<AsylumCase> {

    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");

    private final String templateName;
    private final StringProvider stringProvider;

    public AppealSubmissionTemplate(
        @Value("${appealSubmissionDocument.templateName}") String templateName,
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

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.put("hmcts", "[userImage:hmcts.png]");
        fieldValues.put("CREATED_DATE", caseDetails.getCreatedDate().format(DOCUMENT_DATE_FORMAT));
        fieldValues.put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("appealSubmissionDate", formatDateForRendering(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class).orElse("")));
        fieldValues.put("legalRepresentativeEmailAddress", asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class).orElse(""));
        fieldValues.put("legalRepName", asylumCase.read(LEGAL_REP_NAME, String.class).orElse(""));
        fieldValues.put("legalRepCompany", asylumCase.read(LEGAL_REP_COMPANY, String.class).orElse(""));
        fieldValues.put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""));
        fieldValues.put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""));
        fieldValues.put("appellantDateOfBirth", formatDateForRendering(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class).orElse("")));
        fieldValues.put("appellantTitle", asylumCase.read(APPELLANT_TITLE, String.class).orElse(""));

        Optional<String> homeOfficeDecisionDate = asylumCase.read(HOME_OFFICE_DECISION_DATE, String.class);
        fieldValues.put("homeOfficeDecisionDate", homeOfficeDecisionDate.isPresent() ?
                formatDateForRendering(asylumCase.read(HOME_OFFICE_DECISION_DATE, String.class).orElse("")) : null);

        Optional<String> decisionLetterReceivedDate = asylumCase.read(DECISION_LETTER_RECEIVED_DATE, String.class);
        fieldValues.put("decisionLetterReceivedDate", decisionLetterReceivedDate.isPresent() ?
                formatDateForRendering(asylumCase.read(DECISION_LETTER_RECEIVED_DATE, String.class).orElse("")) : null);

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

        if (asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {

            Optional<OutOfCountryDecisionType> maybeOutOfCountryDecisionType = asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class);

            if (maybeOutOfCountryDecisionType.isPresent()) {

                OutOfCountryDecisionType decisionType = maybeOutOfCountryDecisionType.get();
                fieldValues.put("outOfCountryDecisionType", maybeOutOfCountryDecisionType.get().getDescription());
                fieldValues.put("decisionLetterReceived", YesOrNo.YES);

                if (decisionType == OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS || decisionType == OutOfCountryDecisionType.REFUSE_PERMIT) {
                    fieldValues.put("gwfReferenceNumber", asylumCase.read(GWF_REFERENCE_NUMBER, String.class).orElse(null));
                    fieldValues.put("dateEntryClearanceDecision", formatDateForRendering(asylumCase.read(DATE_ENTRY_CLEARANCE_DECISION, String.class).orElse(null)));
                    fieldValues.put("decisionLetterReceived", YesOrNo.NO);

                } else if (decisionType == OutOfCountryDecisionType.REFUSAL_OF_PROTECTION) {
                    fieldValues.put("dateClientLeaveUk", formatDateForRendering(asylumCase.read(DATE_CLIENT_LEAVE_UK, String.class).orElse(null)));
                    fieldValues.put("didClientLeaveUk", YesOrNo.YES);
                }
            }

            if (asylumCase.read(HAS_CORRESPONDENCE_ADDRESS, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
                fieldValues.put("appellantOutOfCountryAddress", asylumCase.read(APPELLANT_OUT_OF_COUNTRY_ADDRESS, String.class).orElse(""));
            }

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

        Optional<String> optionalAppealType = asylumCase.read(APPEAL_TYPE);

        if (optionalAppealType.isPresent()) {
            String appealType = optionalAppealType.get();
            fieldValues.put(
                "appealType",
                stringProvider.get("appealType", appealType).orElse("")
            );
        }

        YesOrNo removalOrderOption = asylumCase.read(REMOVAL_ORDER_OPTIONS, YesOrNo.class).orElse(YesOrNo.NO);
        if (removalOrderOption.equals(YesOrNo.YES)) {
            fieldValues.put("removalOrderDate", asylumCase.read(REMOVAL_ORDER_DATE, String.class).orElse(""));
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

        fieldValues.put(
            "otherAppeals",
            otherAppeals
                .orElse(Collections.emptyList())
                .stream()
                .filter(idValue -> idValue.getValue().containsKey("value"))
                .map(idValue -> idValue.getValue().get("value"))
                .collect(joining(", "))
        );

        fieldValues.put("applicationOutOfTimeExplanation", asylumCase.read(APPLICATION_OUT_OF_TIME_EXPLANATION, String.class).orElse(""));
        fieldValues.put("submissionOutOfTime", asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class).orElse(YesOrNo.NO));
        fieldValues.put(
            "applicationOutOfTimeDocumentName",
            asylumCase.read(APPLICATION_OUT_OF_TIME_DOCUMENT, Document.class)
                .map(Document::getDocumentFilename)
                .orElse("")
        );

        return fieldValues;
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
                String prisonerNomsNumberField = asylumCase
                        .get("prisonNOMSNumber")
                        .toString();

                String prisonerNomsNumberValue = "";
                if (!prisonerNomsNumberField.isEmpty()) {
                    prisonerNomsNumberValue = formatComplexString(prisonerNomsNumberField);
                    nomsAvailable = YesOrNo.YES;
                    fieldValues.put("nomsNumber", prisonerNomsNumberValue);
                }
                fieldValues.put("nomsAvailable", nomsAvailable);

                YesOrNo releaseDateProvided = YesOrNo.NO;
                String prisonerReleaseDateField = asylumCase
                        .get("dateCustodialSentence")
                        .toString();

                String prisonerReleaseDateValue = "";
                if (!prisonerReleaseDateField.isEmpty()) {
                    prisonerReleaseDateValue = formatComplexString(prisonerReleaseDateField);
                    releaseDateProvided = YesOrNo.YES;
                    fieldValues.put("releaseDate", prisonerReleaseDateValue);
                }
                fieldValues.put("releaseDateProvided", releaseDateProvided);
                break;
            case "other":
                detentionFacility = "Other";
                detentionFacilityName = asylumCase.get("otherDetentionFacilityName").toString().replaceAll("[\\[\\](){}]","");
                detentionFacilityName = detentionFacilityName.substring(detentionFacilityName.lastIndexOf("=") + 1);
                break;
            default:
                // Required for sonar scan. Can never reach here.
        }
        fieldValues.put("detentionFacility", detentionFacility);
        fieldValues.put("detentionFacilityName", detentionFacilityName);

        BailApplicationStatus hasBailApplication = asylumCase.read(HAS_PENDING_BAIL_APPLICATIONS, BailApplicationStatus.class).orElse(BailApplicationStatus.NO);
        fieldValues.put("hasPendingBailApplication", hasBailApplication);
        if (hasBailApplication.equals(BailApplicationStatus.YES)) {
            fieldValues.put("bailApplicationNumber", asylumCase.read(BAIL_APPLICATION_NUMBER, String.class).orElse(""));
        }
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

    private String formatDateForRendering(String date) {
        if (!Strings.isNullOrEmpty(date)) {
            return LocalDate.parse(date).format(DOCUMENT_DATE_FORMAT);
        }
        return "";
    }

    protected static String formatComplexString(String data) {
        data = data.replaceAll("[\\[\\](){}]", "");
        return data.substring(data.lastIndexOf("=") + 1);
    }

}
