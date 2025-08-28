package uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumAppealType.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionType.HO_WAIVER_REMISSION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionType.NO_REMISSION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;


@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class AsylumCaseUtilsTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private Document document;
    @Mock
    private AddressUk address;
    private final String directionExplanation = "some explanation";
    private final Parties directionParties = Parties.APPELLANT;
    private final String directionDateDue = "2023-06-16";
    private final String directionDateSent = "2023-06-02";
    private final String directionUniqueId = "95e90870-2429-4660-b9c2-4111aff37304";
    private final String directionType = "someDirectionType";
    private final IdValue<Direction> requestCaseBuildingDirection = new IdValue<>(
            "1",
            new Direction(
                    directionExplanation,
                    directionParties,
                    directionDateDue,
                    directionDateSent,
                    DirectionTag.REQUEST_CASE_BUILDING,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    directionUniqueId,
                    directionType
            )
    );

    private final IdValue<Direction> caseEditDirection = new IdValue<>(
            "2",
            new Direction(
                    directionExplanation,
                    directionParties,
                    directionDateDue,
                    directionDateSent,
                    DirectionTag.CASE_EDIT,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    directionUniqueId,
                    directionType
            )
    );

    private final IdValue<Direction> adaListCaseDirection = new IdValue<>(
            "3",
            new Direction(
                    directionExplanation,
                    directionParties,
                    directionDateDue,
                    directionDateSent,
                    DirectionTag.ADA_LIST_CASE,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    directionUniqueId,
                    directionType
            )
    );

    private final String legalOfficerAddendumUploadedByLabel = "TCW";
    private final String legalOfficerAddendumUploadSuppliedByLabel = "The respondent";
    private final IdValue<DocumentWithMetadata> addendumOne = new IdValue<>(
            "1",
            new DocumentWithMetadata(
                    document,
                    "Some description",
                    "2018-12-25", DocumentTag.ADDENDUM_EVIDENCE,
                    legalOfficerAddendumUploadSuppliedByLabel,
                    legalOfficerAddendumUploadedByLabel
            )
    );

    private final IdValue<DocumentWithMetadata> addendumTwo = new IdValue<>(
            "2",
            new DocumentWithMetadata(
                    document,
                    "Some description",
                    "2018-12-26", DocumentTag.ADDENDUM_EVIDENCE,
                    legalOfficerAddendumUploadSuppliedByLabel,
                    legalOfficerAddendumUploadedByLabel
            )
    );

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class)
    void should_return_correct_value_for_isAda(YesOrNo yesOrNo) {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(yesOrNo));

        if (yesOrNo.equals(YES)) {
            assertTrue(AsylumCaseUtils.isAcceleratedDetainedAppeal(asylumCase));
        } else {
            assertFalse(AsylumCaseUtils.isAcceleratedDetainedAppeal(asylumCase));
        }
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class)
    void should_return_correct_value_for_is_detained(YesOrNo yesOrNo) {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(yesOrNo));

        if (yesOrNo.equals(YES)) {
            assertTrue(AsylumCaseUtils.isAppellantInDetention(asylumCase));
        } else {
            assertFalse(AsylumCaseUtils.isAppellantInDetention(asylumCase));
        }
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class)
    void should_return_correct_value_for_isAdmin(YesOrNo yesOrNo) {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(yesOrNo));

        if (yesOrNo.equals(YES)) {
            assertTrue(AsylumCaseUtils.isInternalCase(asylumCase));
        } else {
            assertFalse(AsylumCaseUtils.isInternalCase(asylumCase));
        }
    }

    @Test
    void should_return_true_for_internal_non_detained_case() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertTrue(AsylumCaseUtils.isInternalNonDetainedCase(asylumCase));
    }

    @Test
    void should_return_empty_list_when_no_case_directions_present() {
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.empty());

        assertEquals(Collections.emptyList(), AsylumCaseUtils.getCaseDirections(asylumCase));
    }

    @Test
    void should_return_empty_list_when_no_directions_found_based_on_direction_tag() {
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.empty());

        assertEquals(Collections.emptyList(), AsylumCaseUtils.getCaseDirectionsBasedOnTag(asylumCase, DirectionTag.REQUEST_CASE_BUILDING));
    }

    @Test
    void should_return_direction_list() {
        List<IdValue<Direction>> directionList = new ArrayList<>();
        directionList.add(requestCaseBuildingDirection);
        directionList.add(caseEditDirection);
        directionList.add(adaListCaseDirection);
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(directionList));

        List<IdValue<Direction>> returnedDirectionList = AsylumCaseUtils.getCaseDirections(asylumCase);

        MatcherAssert.assertThat(directionList, IsIterableContainingInOrder.contains(returnedDirectionList.toArray()));

    }

    @ParameterizedTest
    @EnumSource(value = DirectionTag.class, names = {"REQUEST_CASE_BUILDING", "CASE_EDIT", "ADA_LIST_CASE"})
    void should_return_specific_direction_based_on_tag(DirectionTag directionTag) {
        List<IdValue<Direction>> directionList = new ArrayList<>();
        directionList.add(requestCaseBuildingDirection);
        directionList.add(caseEditDirection);
        directionList.add(adaListCaseDirection);
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(directionList));

        List<Direction> returnedDirection = AsylumCaseUtils.getCaseDirectionsBasedOnTag(asylumCase, directionTag);

        assertEquals(1, returnedDirection.size());

        assertEquals(directionTag, returnedDirection.get(0).getTag());

    }

    @ParameterizedTest
    @EnumSource(value = AsylumAppealType.class)
    void should_return_true_if_ea_hu_eu_appeal_type(AsylumAppealType appealType) {
        when(asylumCase.read(APPEAL_TYPE, AsylumAppealType.class)).thenReturn(Optional.of(appealType));
        if (List.of(HU, EA, EU).contains(appealType)) {
            assertTrue(AsylumCaseUtils.isEaHuEuAppeal(asylumCase));
        } else {
            assertFalse(AsylumCaseUtils.isEaHuEuAppeal(asylumCase));
        }
    }

    @Test
    void should_throw_for_fee_amount_not_present() {
        assertThatThrownBy(() -> AsylumCaseUtils.getFeeBeforeRemission(asylumCase))
                .isExactlyInstanceOf(RequiredFieldMissingException.class)
                .hasMessage("Fee amount not found");
    }

    @Test
    void should_return_fee_amount() {
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of("14000"));
        assertEquals(140, AsylumCaseUtils.getFeeBeforeRemission(asylumCase));
    }

    @Test
    void should_throw_for_remission_type_not_present() {
        assertThatThrownBy(() -> AsylumCaseUtils.getFeeRemission(asylumCase))
                .isExactlyInstanceOf(RequiredFieldMissingException.class)
                .hasMessage("Remission type not found");
    }

    @ParameterizedTest
    @EnumSource(value = RemissionType.class)
    void should_return_amount_remitted(RemissionType remissionType) {
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(remissionType));
        if (remissionType.equals(NO_REMISSION)) {
            assertEquals(0, AsylumCaseUtils.getFeeRemission(asylumCase));
        } else {
            when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("8000"));
            assertEquals(80, AsylumCaseUtils.getFeeRemission(asylumCase));
        }
    }

    @Test
    void should_return_no_amount_remitted_for_rejected_remission_decision() {
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(RemissionDecision.REJECTED));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(HO_WAIVER_REMISSION));

        when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("0"));
        assertEquals(0, AsylumCaseUtils.getFeeRemission(asylumCase));
    }

    @Test
    void should_throw_for_amount_remitted_not_present() {
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        assertThatThrownBy(() -> AsylumCaseUtils.getFeeRemission(asylumCase))
                .isExactlyInstanceOf(RequiredFieldMissingException.class)
                .hasMessage("Amount remitted not found");
    }

    @Test
    void should_get_addendum_document_when_present() {
        List<IdValue<DocumentWithMetadata>> addendumDocuments = new ArrayList<>();
        addendumDocuments.add(addendumOne);
        when(asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(addendumDocuments));

        assertEquals(addendumDocuments, AsylumCaseUtils.getAddendumEvidenceDocuments(asylumCase));
        assertEquals(Optional.of(addendumOne), AsylumCaseUtils.getLatestAddendumEvidenceDocument(asylumCase));
    }

    @Test
    void should_get_addendum_documents_when_more_than_one_exists() {
        List<IdValue<DocumentWithMetadata>> addendumDocuments = new ArrayList<>();
        addendumDocuments.add(addendumOne);
        addendumDocuments.add(addendumTwo);
        when(asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(addendumDocuments));

        assertEquals(addendumDocuments, AsylumCaseUtils.getAddendumEvidenceDocuments(asylumCase));
        assertEquals(2, AsylumCaseUtils.getAddendumEvidenceDocuments(asylumCase).size());
    }

    @Test
    void should_return_empty_list_when_no_addendum_evidence_documents_present() {
        when(asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS)).thenReturn(Optional.empty());

        assertEquals(Collections.emptyList(), AsylumCaseUtils.getAddendumEvidenceDocuments(asylumCase));
        assertEquals(Optional.empty(), AsylumCaseUtils.getLatestAddendumEvidenceDocument(asylumCase));
    }

    @ParameterizedTest
    @ValueSource(strings = { "LEGAL_REPRESENTATIVE", "RESPONDENT", "APPELLANT", "BOTH", "APPELLANT_AND_RESPONDENT" })
    void should_return_correct_value_for_is_direction_party_respondent(String party) {
        when(asylumCase.read(DIRECTION_EDIT_PARTIES, Parties.class)).thenReturn(Optional.of(Parties.valueOf(party)));

        if (party.equals("RESPONDENT")) {
            assertTrue(AsylumCaseUtils.isDirectionPartyRespondent(asylumCase));
        } else {
            assertFalse(AsylumCaseUtils.isDirectionPartyRespondent(asylumCase));
        }
    }

    @Test
    void should_throw_if_direction_edit_date_due_not_present() {
        when(asylumCase.read(DIRECTION_EDIT_DATE_DUE, String.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> AsylumCaseUtils.getDirectionDueDateAndExplanation(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Direction edit date due is not present");
    }

    @Test
    void should_throw_if_direction_edit_explanation_not_present() {
        when(asylumCase.read(DIRECTION_EDIT_DATE_DUE, String.class)).thenReturn(Optional.of("2020-12-25"));
        when(asylumCase.read(DIRECTION_EDIT_EXPLANATION, String.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> AsylumCaseUtils.getDirectionDueDateAndExplanation(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Direction edit explanation is not present");
    }

    @Test
    void should_return_isDecisionWithoutHearingAppeal() {
        when(asylumCase.read(IS_DECISION_WITHOUT_HEARING, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(AsylumCaseUtils.isDecisionWithoutHearingAppeal(asylumCase));
    }

    @Test
    void should_return_isRemoteHearing() {
        when(asylumCase.read(IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(AsylumCaseUtils.isRemoteHearing(asylumCase));
    }

    @Test
    void should_return_isVirtualHearing() {
        when(asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(AsylumCaseUtils.isVirtualHearing(asylumCase));
    }

    @Test
    void should_return_isVirtualHearing_false_when_value_is_no() {
        when(asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class)).thenReturn(Optional.of(NO));
        assertFalse(AsylumCaseUtils.isVirtualHearing(asylumCase));
    }

    @Test
    void should_return_isVirtualHearing_true_when_list_hearing_centre_is_virtual() {
        when(asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.of(HearingCentre.IAC_NATIONAL_VIRTUAL));
        assertTrue(AsylumCaseUtils.isVirtualHearing(asylumCase));
    }

    @Test
    void should_return_isVirtualHearing_false_when_missing_in_case_data() {
        when(asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class)).thenReturn(Optional.empty());
        assertFalse(AsylumCaseUtils.isVirtualHearing(asylumCase));
    }

    @Test
    void should_return_isVirtualHearing_true_when_missing_in_case_data__and_hearing_centre_not_virtual() {
        when(asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.of(HearingCentre.GLASGOW_TRIBUNAL_CENTRE));
        assertFalse(AsylumCaseUtils.isVirtualHearing(asylumCase));
    }

    @Test
    void should_return_isVirtualHearing_true_when_isVirtualHearing_missing_and_hearing_centre_virtual() {
        when(asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.of(HearingCentre.IAC_NATIONAL_VIRTUAL));
        assertTrue(AsylumCaseUtils.isVirtualHearing(asylumCase));
    }

    @Test
    void should_return_address_with_all_fields_populated() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.of(address));
        when(address.getAddressLine1()).thenReturn(Optional.of("Apartment 99"));
        when(address.getAddressLine2()).thenReturn(Optional.of("Example Road"));
        when(address.getAddressLine3()).thenReturn(Optional.of("Example County"));
        when(address.getPostTown()).thenReturn(Optional.of("Example Town"));
        when(address.getPostCode()).thenReturn(Optional.of("PostCode"));

        List<String> result = AsylumCaseUtils.getAppellantAddressAsList(asylumCase);

        assertEquals(5, result.size());
        assertEquals("Apartment 99", result.get(0));
        assertEquals("Example Road", result.get(1));
        assertEquals("Example County", result.get(2));
        assertEquals("Example Town", result.get(3));
        assertEquals("PostCode", result.get(4));
    }

    @Test
    void should_throw_when_address_is_not_present() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            AsylumCaseUtils.getAppellantAddressAsList(asylumCase);
        }, "appellantAddress is not present");
    }

    @Test
    void hasAppellantAddressInCountryOrOoc_should_return_true_for_in_country() {
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(AsylumCaseUtils.hasAppellantAddressInCountryOrOoc(asylumCase));
    }

    @Test
    void hasAppellantAddressInCountryOrOoc_should_return_true_for_out_of_country() {
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.empty());

        assertTrue(AsylumCaseUtils.hasAppellantAddressInCountryOrOoc(asylumCase));
    }

    @Test
    void should_return_appellant_in_uk() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YES));

        boolean result = AsylumCaseUtils.isAppellantInUk(asylumCase);

        assertEquals(true, result);
    }

    @Test
    void should_calculate_fee_difference_correctly() {
        String originalFee = "15000";
        String newFee = "10000";

        String feeDifference = AsylumCaseUtils.calculateFeeDifference(originalFee, newFee);

        assertEquals("50.00", feeDifference);
    }

    @Test
    void should_return_zero_when_original_fee_is_invalid() {
        String originalFee = "invalid";
        String newFee = "10000";

        String feeDifference = AsylumCaseUtils.calculateFeeDifference(originalFee, newFee);

        assertEquals("0.00", feeDifference);
    }

    @Test
    void should_return_zero_when_new_fee_is_invalid() {
        String originalFee = "15000";
        String newFee = "invalid";

        String feeDifference = AsylumCaseUtils.calculateFeeDifference(originalFee, newFee);

        assertEquals("0.00", feeDifference);
    }

    @Test
    void should_return_zero_when_both_fees_are_invalid() {
        String originalFee = "invalid";
        String newFee = "invalid";

        String feeDifference = AsylumCaseUtils.calculateFeeDifference(originalFee, newFee);

        assertEquals("0.00", feeDifference);
    }


    @Test
    void should_convert_asylum_case_fee_value_correctly() {
        String feeValue = "12345";

        String convertedFeeValue = AsylumCaseUtils.convertAsylumCaseFeeValue(feeValue);

        assertEquals("123.45", convertedFeeValue);
    }


    @Test
    void should_return_empty_string_for_blank_fee_value_input() {
        String feeValue = "";

        String convertedFeeValue = AsylumCaseUtils.convertAsylumCaseFeeValue(feeValue);

        assertEquals("", convertedFeeValue);
    }

    @Test
    void should_return_empty_string_for_null_fee_value_input() {

        String convertedFeeValue = AsylumCaseUtils.convertAsylumCaseFeeValue(null);

        assertEquals("", convertedFeeValue);
    }

    @Test
    void should_return_true_when_appellant_is_in_detention_and_facility_type_matches() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));

        assertTrue(AsylumCaseUtils.isDetainedInFacilityType(asylumCase, DetentionFacility.IRC));
    }

    @Test
    void should_return_false_when_appellant_is_not_in_detention() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertFalse(AsylumCaseUtils.isDetainedInFacilityType(asylumCase, DetentionFacility.IRC));
    }

    @Test
    void should_return_false_when_facility_type_does_not_match() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));

        assertFalse(AsylumCaseUtils.isDetainedInFacilityType(asylumCase, DetentionFacility.IRC));
    }

    @Test
    void should_return_false_when_detention_facility_is_empty() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());

        assertFalse(AsylumCaseUtils.isDetainedInFacilityType(asylumCase, DetentionFacility.IRC));
    }

    @ParameterizedTest
    @CsvSource({
        "immigrationRemovalCentre, IRC",
        "prison, PRISON",
        "other, OTHER"
    })
    void should_return_true_for_all_facility_types_when_appellant_is_detained(String detentionFacilityValue, DetentionFacility facilityType) {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(detentionFacilityValue));

        assertTrue(AsylumCaseUtils.isDetainedInFacilityType(asylumCase, facilityType));
    }

    @Test
    void should_return_true_for_legal_rep_case_for_detained_appellant() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));

        assertTrue(AsylumCaseUtils.isLegalRepCaseForDetainedAppellant(asylumCase));
    }

    @Test
    void should_return_false_for_internal_case_with_detained_appellant() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));

        assertFalse(AsylumCaseUtils.isLegalRepCaseForDetainedAppellant(asylumCase));
    }

    @Test
    void should_return_false_for_non_internal_case_with_non_detained_appellant() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertFalse(AsylumCaseUtils.isLegalRepCaseForDetainedAppellant(asylumCase));
    }

    @Test
    void should_return_false_for_internal_case_with_non_detained_appellant() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertFalse(AsylumCaseUtils.isLegalRepCaseForDetainedAppellant(asylumCase));
    }

    @ParameterizedTest
    @CsvSource({
        "YES, YES, false",
        "YES, NO, false", 
        "NO, YES, true",
        "NO, NO, false"
    })
    void should_return_correct_value_for_legal_rep_case_for_detained_appellant_combinations(YesOrNo isAdmin, YesOrNo isDetained, boolean expected) {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(isAdmin));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(isDetained));

        assertEquals(expected, AsylumCaseUtils.isLegalRepCaseForDetainedAppellant(asylumCase));
    }

}