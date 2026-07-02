package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.OutOfCountryCircumstances;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.OutOfCountryDecisionType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HasOtherAppeals;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.stream.Collectors.joining;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.OutOfCountryCircumstances.ENTRY_CLEARANCE_DECISION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.OutOfCountryCircumstances.LEAVE_UK;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_OUT_OF_COUNTRY_ADDRESS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DATE_CLIENT_LEAVE_UK;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DATE_ENTRY_CLEARANCE_DECISION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.GWF_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HAS_CORRESPONDENCE_ADDRESS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HAS_OTHER_APPEALS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HAS_SPONSOR;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.OOC_APPEAL_ADMIN_J;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.OUT_OF_COUNTRY_DECISION_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SPONSOR_ADDRESS_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SPONSOR_CONTACT_PREFERENCE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SPONSOR_EMAIL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SPONSOR_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SPONSOR_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SPONSOR_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.formatDateForRendering;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAppellantInUk;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isInternalCase;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.APPEAL_RECEIVED_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.APPELLANT_FULL_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.DAYS_14;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.DAYS_14_FROM_DATE_OF_DIRECTION_KEY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.DAYS_42;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.DAYS_42_FROM_DATE_OF_DIRECTION_KEY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.DAYS_56;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.DAYS_56_FROM_DATE_OF_DIRECTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.DECISION_SENT_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.D_MMM_YYYY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.PRACTICE_DIRECTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.WEEKS_DEADLINE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.getAppellantFamilyName;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.getAppellantGivenName;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.populateStatutoryTimeFrame24wDate;

@Service
public class Stf24WeeksCaseReviewDocFieldMapper {

    public static final String CIRCUMSTANCES_OF_THE_APPELLANT_S_OUT_OF_COUNTRY_APPEAL_TITLE = "Circumstances of the appellant's out of country appeal";
    public static final String THE_APPELLANT_IS_APPEALING_AN_ENTRY_CLEARANCE_DECISION = "The appellant is appealing an entry clearance decision";
    public static final String THE_APPELLANT_HAD_TO_LEAVE_THE_UK_IN_ORDER_TO_APPEAL = "The appellant had to leave the UK in order to appeal";
    public static final String OUT_OF_COUNTRY_DECISION_TYPE_TITLE = "outOfCountryDecisionTypeTitle";
    public static final String OUT_OF_COUNTRY_DECISION_TYPE_TEXT = "outOfCountryDecisionType";
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Stf24WeeksCaseReviewDocFieldMapper.class);
    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = ofPattern("ddMMyyyy");
    private final StringProvider stringProvider;

    public Stf24WeeksCaseReviewDocFieldMapper(StringProvider stringProvider) {
        this.stringProvider = stringProvider;
    }

    static void populateOtherAppeals(Map<String, Object> fieldValues, Optional<List<IdValue<Map<String, String>>>> otherAppeals, AsylumCase asylumCase) {
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

    static void addAppealOocFields(AsylumCase asylumCase, Map<String, Object> fieldValues) {
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

    static void populateSponsorFields(AsylumCase asylumCase, Map<String, Object> fieldValues) {
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


    public Map<String, Object> mapFieldValues(
            CaseDetails<AsylumCase> caseDetails) {
        final AsylumCase asylumCase = caseDetails.getCaseData();
        LOGGER.info("Mapping field values for case ID: {}", caseDetails.getId());
        final Map<String, Object> fieldValues = new HashMap<>();
        /// STF 24w
        LocalDate now = LocalDate.now();
        fieldValues.put(PRACTICE_DIRECTION, now.format(ofPattern(D_MMM_YYYY)));
        fieldValues.put(DAYS_14_FROM_DATE_OF_DIRECTION_KEY, now.plusDays(DAYS_14).format(ofPattern(D_MMM_YYYY)));
        fieldValues.put(DAYS_42_FROM_DATE_OF_DIRECTION_KEY, now.plusDays(DAYS_42).format(ofPattern(D_MMM_YYYY)));
        fieldValues.put(DAYS_56_FROM_DATE_OF_DIRECTION, now.plusDays(DAYS_56).format(ofPattern(D_MMM_YYYY)));
        String familyName = getAppellantFamilyName(asylumCase);
        String givenNames = getAppellantGivenName(asylumCase);

        fieldValues.put("appellantGivenNames", givenNames);
        fieldValues.put("appellantFamilyName", familyName);
        fieldValues.put(APPELLANT_FULL_NAME, (givenNames + " " + familyName).trim());
        fieldValues.put(WEEKS_DEADLINE, populateStatutoryTimeFrame24wDate(asylumCase));
        fieldValues.put(DECISION_SENT_DATE, Stf24WeeksUtils.getHomeOfficeDecisionDate(asylumCase));
        fieldValues.put(APPEAL_RECEIVED_DATE, Stf24WeeksUtils.getAppealReceivedDate(asylumCase));
        /// STF24W

        return fieldValues;
    }


}
