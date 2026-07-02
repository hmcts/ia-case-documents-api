package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class Stf24WeeksCaseReviewDocFieldMapperTest {
    @Mock
    private StringProvider stringProvider;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private AsylumCase asylumCase;

    private Stf24WeeksCaseReviewDocFieldMapper fieldMapper;

    @BeforeEach
    void setUp() {
        fieldMapper = new Stf24WeeksCaseReviewDocFieldMapper(stringProvider);
    }

    @Test
    void should_map_all_stf24_week_field_values_when_all_data_present() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(123L);
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
                .thenReturn(Optional.of("John"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.of("Smith"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.of("2023-01-01"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_DECISION_DATE, String.class))
                .thenReturn(Optional.of("2022-12-01"));

        Map<String, Object> result = fieldMapper.mapFieldValues(caseDetails);

        assertTrue(result.containsKey("practiceDirection"));
        assertTrue(result.containsKey("14DaysFromDateOfDirection"));
        assertTrue(result.containsKey("42DaysFromDateOfDirection"));
        assertTrue(result.containsKey("56DaysFromDateOfDirection"));
        assertEquals("John", result.get("appellantGivenNames"));
        assertEquals("Smith", result.get("appellantFamilyName"));
        assertEquals("John Smith", result.get("appellantFullName"));
        assertEquals("18 Jun 2023", result.get("24WeeksDeadline"));
        assertEquals("1 Dec 2022", result.get("decisionSentDate"));
        assertTrue(result.containsKey("appealReceivedDate"));
    }

    @Test
    void should_handle_empty_appellant_names_and_trim_full_name() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(124L);
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.of("2023-01-01"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_DECISION_DATE, String.class))
                .thenReturn(Optional.of("2022-12-01"));

        Map<String, Object> result = fieldMapper.mapFieldValues(caseDetails);

        assertEquals("", result.get("appellantGivenNames"));
        assertEquals("", result.get("appellantFamilyName"));
        assertEquals("", result.get("appellantFullName"));
    }

    @Test
    void should_handle_only_given_names_and_trim_full_name() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(125L);
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
                .thenReturn(Optional.of("John"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.of("2023-01-01"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_DECISION_DATE, String.class))
                .thenReturn(Optional.of("2022-12-01"));

        Map<String, Object> result = fieldMapper.mapFieldValues(caseDetails);

        assertEquals("John", result.get("appellantFullName"));
    }

    @Test
    void should_handle_only_family_names_and_trim_full_name() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(126L);
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.of("Smith"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.of("2023-01-01"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_DECISION_DATE, String.class))
                .thenReturn(Optional.of("2022-12-01"));

        Map<String, Object> result = fieldMapper.mapFieldValues(caseDetails);

        assertEquals("Smith", result.get("appellantFullName"));
    }

    @Test
    void should_calculate_correct_deadline_dates_from_today() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(127L);
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
                .thenReturn(Optional.of("Jane"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.of("Doe"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.of("2023-01-01"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_DECISION_DATE, String.class))
                .thenReturn(Optional.of("2022-12-01"));

        Map<String, Object> result = fieldMapper.mapFieldValues(caseDetails);

        LocalDate today = LocalDate.now();
        String expected14Days = today.plusDays(14).format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy"));
        String expected42Days = today.plusDays(42).format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy"));
        String expected56Days = today.plusDays(56).format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy"));

        assertEquals(expected14Days, result.get("14DaysFromDateOfDirection"));
        assertEquals(expected42Days, result.get("42DaysFromDateOfDirection"));
        assertEquals(expected56Days, result.get("56DaysFromDateOfDirection"));
    }

    @Test
    void should_populate_other_appeals_with_empty_list_when_no_appeals_present() {
        Map<String, Object> fieldValues = new HashMap<>();
        Optional<List<IdValue<Map<String, String>>>> emptyAppeals = Optional.empty();

        Stf24WeeksCaseReviewDocFieldMapper.populateOtherAppeals(fieldValues, emptyAppeals, asylumCase);

        assertEquals("", fieldValues.get("otherAppeals"));
    }

    @Test
    void should_populate_other_appeals_with_has_other_appeals_yes() {
        Map<String, Object> fieldValues = new HashMap<>();
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HAS_OTHER_APPEALS, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HasOtherAppeals.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HasOtherAppeals.YES));

        Stf24WeeksCaseReviewDocFieldMapper.populateOtherAppeals(fieldValues, Optional.empty(), asylumCase);

        assertEquals(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES, fieldValues.get("hasOtherAppeals"));
    }

    @Test
    void should_populate_other_appeals_with_has_other_appeals_no_when_missing() {
        Map<String, Object> fieldValues = new HashMap<>();
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HAS_OTHER_APPEALS, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HasOtherAppeals.class))
                .thenReturn(Optional.empty());

        Stf24WeeksCaseReviewDocFieldMapper.populateOtherAppeals(fieldValues, Optional.empty(), asylumCase);

        assertEquals(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO, fieldValues.get("hasOtherAppeals"));
    }

    @Test
    void should_populate_ooc_fields_when_appeal_out_of_country_is_yes_with_entry_clearance_decision() {
        Map<String, Object> fieldValues = new HashMap<>();
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.OOC_APPEAL_ADMIN_J, uk.gov.hmcts.reform.iacasedocumentsapi.domain.OutOfCountryCircumstances.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.OutOfCountryCircumstances.ENTRY_CLEARANCE_DECISION));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_UK, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO));

        Stf24WeeksCaseReviewDocFieldMapper.addAppealOocFields(asylumCase, fieldValues);

        assertEquals("Circumstances of the appellant's out of country appeal", fieldValues.get("outOfCountryDecisionTypeTitle"));
        assertEquals("The appellant is appealing an entry clearance decision", fieldValues.get("outOfCountryDecisionType"));
    }

    @Test
    void should_populate_ooc_fields_when_appeal_out_of_country_is_yes_with_leave_uk() {
        Map<String, Object> fieldValues = new HashMap<>();
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.OOC_APPEAL_ADMIN_J, uk.gov.hmcts.reform.iacasedocumentsapi.domain.OutOfCountryCircumstances.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.OutOfCountryCircumstances.LEAVE_UK));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_UK, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO));

        Stf24WeeksCaseReviewDocFieldMapper.addAppealOocFields(asylumCase, fieldValues);

        assertEquals("Circumstances of the appellant's out of country appeal", fieldValues.get("outOfCountryDecisionTypeTitle"));
        assertEquals("The appellant had to leave the UK in order to appeal", fieldValues.get("outOfCountryDecisionType"));
    }

    @Test
    void should_not_populate_ooc_fields_when_appeal_out_of_country_is_no() {
        Map<String, Object> fieldValues = new HashMap<>();
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO));

        Stf24WeeksCaseReviewDocFieldMapper.addAppealOocFields(asylumCase, fieldValues);

        assertFalse(fieldValues.containsKey("outOfCountryDecisionTypeTitle"));
        assertFalse(fieldValues.containsKey("outOfCountryDecisionType"));
    }

    @Test
    void should_populate_sponsor_fields_when_has_sponsor_is_yes_and_email_preference() {
        Map<String, Object> fieldValues = new HashMap<>();
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HAS_SPONSOR, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SPONSOR_GIVEN_NAMES, String.class))
                .thenReturn(Optional.of("Jane"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SPONSOR_FAMILY_NAME, String.class))
                .thenReturn(Optional.of("Sponsor"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SPONSOR_ADDRESS_FOR_DISPLAY, String.class))
                .thenReturn(Optional.of("123 Main St"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SPONSOR_CONTACT_PREFERENCE, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ContactPreference.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ContactPreference.WANTS_EMAIL));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SPONSOR_EMAIL, String.class))
                .thenReturn(Optional.of("sponsor@example.com"));

        Stf24WeeksCaseReviewDocFieldMapper.populateSponsorFields(asylumCase, fieldValues);

        assertEquals(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES, fieldValues.get("hasSponsor"));
        assertEquals("Jane", fieldValues.get("sponsorGivenNames"));
        assertEquals("Sponsor", fieldValues.get("sponsorFamilyName"));
        assertEquals("123 Main St", fieldValues.get("sponsorAddress"));
        assertEquals(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES, fieldValues.get("wantsSponsorEmail"));
        assertEquals("sponsor@example.com", fieldValues.get("sponsorEmail"));
    }

    @Test
    void should_populate_sponsor_fields_with_mobile_when_contact_preference_not_email() {
        Map<String, Object> fieldValues = new HashMap<>();
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HAS_SPONSOR, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SPONSOR_GIVEN_NAMES, String.class))
                .thenReturn(Optional.of("Jane"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SPONSOR_FAMILY_NAME, String.class))
                .thenReturn(Optional.of("Sponsor"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SPONSOR_ADDRESS_FOR_DISPLAY, String.class))
                .thenReturn(Optional.of("123 Main St"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SPONSOR_CONTACT_PREFERENCE, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ContactPreference.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SPONSOR_MOBILE_NUMBER, String.class))
                .thenReturn(Optional.of("07123456789"));

        Stf24WeeksCaseReviewDocFieldMapper.populateSponsorFields(asylumCase, fieldValues);

        assertEquals(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES, fieldValues.get("hasSponsor"));
        assertEquals("07123456789", fieldValues.get("sponsorMobileNumber"));
        assertFalse(fieldValues.containsKey("wantsSponsorEmail"));
    }

    @Test
    void should_not_populate_sponsor_fields_when_has_sponsor_is_no() {
        Map<String, Object> fieldValues = new HashMap<>();
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HAS_SPONSOR, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO));

        Stf24WeeksCaseReviewDocFieldMapper.populateSponsorFields(asylumCase, fieldValues);

        assertFalse(fieldValues.containsKey("hasSponsor"));
    }

    @Test
    void should_not_populate_sponsor_fields_when_has_sponsor_missing() {
        Map<String, Object> fieldValues = new HashMap<>();
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HAS_SPONSOR, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.empty());

        Stf24WeeksCaseReviewDocFieldMapper.populateSponsorFields(asylumCase, fieldValues);

        assertFalse(fieldValues.containsKey("hasSponsor"));
    }

    @Test
    void should_populate_appellant_out_of_country_address_when_has_correspondence_address_is_yes() {
        Map<String, Object> fieldValues = new HashMap<>();
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.OUT_OF_COUNTRY_DECISION_TYPE, uk.gov.hmcts.reform.iacasedocumentsapi.domain.OutOfCountryDecisionType.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.OOC_APPEAL_ADMIN_J, uk.gov.hmcts.reform.iacasedocumentsapi.domain.OutOfCountryCircumstances.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HAS_CORRESPONDENCE_ADDRESS, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_OUT_OF_COUNTRY_ADDRESS, String.class))
                .thenReturn(Optional.of("456 Overseas Rd"));

        Stf24WeeksCaseReviewDocFieldMapper.addAppealOocFields(asylumCase, fieldValues);

        assertEquals("456 Overseas Rd", fieldValues.get("appellantOutOfCountryAddress"));
    }

    @Test
    void should_populate_appellant_out_of_country_address_empty_when_not_present() {
        Map<String, Object> fieldValues = new HashMap<>();
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.OUT_OF_COUNTRY_DECISION_TYPE, uk.gov.hmcts.reform.iacasedocumentsapi.domain.OutOfCountryDecisionType.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.OOC_APPEAL_ADMIN_J, uk.gov.hmcts.reform.iacasedocumentsapi.domain.OutOfCountryCircumstances.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HAS_CORRESPONDENCE_ADDRESS, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_OUT_OF_COUNTRY_ADDRESS, String.class))
                .thenReturn(Optional.empty());

        Stf24WeeksCaseReviewDocFieldMapper.addAppealOocFields(asylumCase, fieldValues);

        assertEquals("", fieldValues.get("appellantOutOfCountryAddress"));
    }
}