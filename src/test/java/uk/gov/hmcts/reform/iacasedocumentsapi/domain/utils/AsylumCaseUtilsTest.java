package uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumAppealType.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionType.HO_WAIVER_REMISSION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionType.NO_REMISSION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_HAS_FIXED_ADDRESS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SUBMISSION_OUT_OF_TIME;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.assertj.core.api.AssertionsForClassTypes;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AccessCodeGenerator;


@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class AsylumCaseUtilsTest {

    @Mock
    private AsylumCase asylumCase;
    @Spy
    private AsylumCase asylumCaseSpy;
    @Mock
    private Document document;
    private final MockedStatic<AccessCodeGenerator> generatorMockedStatic = mockStatic(AccessCodeGenerator.class);

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
    private static final String applyForCostsCreationDate = "2023-11-24";
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

    private final String legalRepEmailEjp = "legalRep@example.com";
    private final String generatedCode = "12345";

    @AfterEach
    void tearDown() {
        generatorMockedStatic.close();
    }

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

    @Test
    void should_return_correct_value_for_det() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(AsylumCaseUtils.isAppellantInDetention(asylumCase));
    }

    @Test
    void should_return_correct_value_for_ada() {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(AsylumCaseUtils.isAcceleratedDetainedAppeal(asylumCase));
    }

    @Test
    void should_return_correct_value_for_aaa() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.AG));
        assertTrue(AsylumCaseUtils.isAgeAssessmentAppeal(asylumCase));
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
    void isAdmin_should_return_true() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(AsylumCaseUtils.isInternalCase(asylumCase));
    }

    @Test
    void isAdmin_should_return_false() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(NO));
        assertFalse(AsylumCaseUtils.isInternalCase(asylumCase));
    }

    @Test
    void isNotInternalOrIsInternalWithLegalRepresentation_should_return_true() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertTrue(AsylumCaseUtils.isNotInternalOrIsInternalWithLegalRepresentation(asylumCase));
    }

    @Test
    void isNotInternalOrIsInternalWithLegalRepresentation_should_return_false() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));

        assertFalse(AsylumCaseUtils.isNotInternalOrIsInternalWithLegalRepresentation(asylumCase));
    }

    @Test
    void isAriaMigrated_should_return_true() {
        when(asylumCase.read(IS_ARIA_MIGRATED, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(AsylumCaseUtils.isAriaMigrated(asylumCase));
    }

    @Test
    void isAriaMigrated_should_return_false() {
        when(asylumCase.read(IS_ARIA_MIGRATED, YesOrNo.class)).thenReturn(Optional.of(NO));
        assertFalse(AsylumCaseUtils.isAriaMigrated(asylumCase));
    }

    @Test
    void isAipJourney_should_return_true() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        assertTrue(AsylumCaseUtils.isAipJourney(asylumCase));
    }

    @Test
    void isAipJourney_should_return_false() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        assertFalse(AsylumCaseUtils.isAipJourney(asylumCase));
    }

    @Test
    void getFtpaDecisionOutcomeType_should_return_granted() {
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_GRANTED));
        assertEquals(FtpaDecisionOutcomeType.FTPA_GRANTED, AsylumCaseUtils.getFtpaDecisionOutcomeType(asylumCase).orElse(null));
    }

    @Test
    void getFtpaDecisionOutcomeType_should_return_refused() {
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_REFUSED));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.empty());
        assertEquals(FtpaDecisionOutcomeType.FTPA_REFUSED, AsylumCaseUtils.getFtpaDecisionOutcomeType(asylumCase).orElse(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"birmingham", "", "harmondsworth"})
    void isListed_should_return_correct_value(String hearingCentre) {
        Optional<HearingCentre> mayBeListCaseHearingCenter = HearingCentre.from(hearingCentre);
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(mayBeListCaseHearingCenter);
        assertEquals(mayBeListCaseHearingCenter.isPresent(), AsylumCaseUtils.isAppealListed(asylumCase));
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
    public void testIsLegalRepEjp() {

        Mockito.when(asylumCase.read(LEGAL_REP_REFERENCE_EJP, String.class)).thenReturn(Optional.of(legalRepEmailEjp));
        assertTrue(isLegalRepEjp(asylumCase));
    }

    @Test
    public void testIsNotLegalRepEjp() {
        Mockito.when(asylumCase.read(LEGAL_REP_REFERENCE_EJP, String.class)).thenReturn(Optional.empty());
        assertFalse(isLegalRepEjp(asylumCase));
    }

    @Test
    void should_throw_when_applies_for_costs_are_not_present() {
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.empty());
        AssertionsForClassTypes.assertThatThrownBy(() -> AsylumCaseUtils.retrieveLatestApplyForCosts(asylumCase))
            .hasMessage("Applies for costs are not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_retrieve_latest_created_apply_for_costs() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Wasted costs", "Home office", "Legal representative", applyForCostsCreationDate)),
            new IdValue<>("1", new ApplyForCosts("Unreasonable costs", "Legal representative", "Home office", applyForCostsCreationDate))
        );
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        assertEquals(applyForCostsList.get(0).getValue(), AsylumCaseUtils.retrieveLatestApplyForCosts(asylumCase));
    }

    @Test
    void should_retrieve_application_by_id() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Wasted costs", "Home office", "Legal representative", applyForCostsCreationDate)),
            new IdValue<>("1", new ApplyForCosts("Unreasonable costs", "Legal representative", "Home office", applyForCostsCreationDate))
        );
        DynamicList respondsToCostsList = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(respondsToCostsList));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        assertEquals(applyForCostsList.get(0).getValue(), AsylumCaseUtils.getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST));
    }

    @Test
    void should_throw_if_applies_are_not_present() {
        DynamicList respondsToCostsList = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(respondsToCostsList));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.empty());

        AssertionsForClassTypes.assertThatThrownBy(() -> AsylumCaseUtils.getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST))
            .hasMessage("appliesForCost are not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_if_application_is_not_found_by_id() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Wasted costs", "Home office", "Legal representative", applyForCostsCreationDate)),
            new IdValue<>("1", new ApplyForCosts("Unreasonable costs", "Legal representative", "Home office", applyForCostsCreationDate))
        );
        DynamicList respondsToCostsList = new DynamicList(new Value("3", "Costs 3, Wasted costs, 24 Nov 2023"), List.of(new Value("3", "Costs 3, Wasted costs, 24 Nov 2023")));

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(respondsToCostsList));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        AssertionsForClassTypes.assertThatThrownBy(() -> AsylumCaseUtils.getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST))
            .hasMessage("Apply for costs with id 3 not found")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_check_if_logged_user_is_home_office() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Home office", "Wasted costs"))
        );
        DynamicList respondsToCostsList = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(respondsToCostsList));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        assertTrue(AsylumCaseUtils.isLoggedUserIsHomeOffice(asylumCase, testFunc -> AsylumCaseUtils.getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST)));
    }

    @Test
    void should_throw_if_logged_user_is__of_incorrect_type() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Tribunal", "Wasted costs"))
        );
        DynamicList respondsToCostsList = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(respondsToCostsList));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        AssertionsForClassTypes.assertThatThrownBy(() -> AsylumCaseUtils.isLoggedUserIsHomeOffice(asylumCase, testFunc -> AsylumCaseUtils.getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST)))
            .hasMessage("Correct applicant type is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_build_proper_pair_with_applicant_and_respondent() {
        DynamicList selectedValue = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Wasted costs", "Legal representative", "Home office", applyForCostsCreationDate))
        );

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(selectedValue));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        ImmutablePair<String, String> getApplicantAndRespondent = getApplicantAndRespondent(asylumCase, testFunc -> AsylumCaseUtils.getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST));

        assertEquals("Legal representative", getApplicantAndRespondent.getRight());
        assertEquals("Home office", getApplicantAndRespondent.getLeft());
    }

    @Test
    void should_throw_if_applicant_type_is_not_correct() {
        DynamicList selectedValue = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Wasted costs", "Tribunal", "Case officer", applyForCostsCreationDate))
        );

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(selectedValue));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        AssertionsForClassTypes.assertThatThrownBy(() -> getApplicantAndRespondent(asylumCase, testFunc -> AsylumCaseUtils.getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST)))
            .hasMessage("Correct applicant type is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_if_respondent_type_is_not_correct() {
        DynamicList selectedValue = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Wasted costs", "Case officer", "Tribunal", applyForCostsCreationDate))
        );

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(selectedValue));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        AssertionsForClassTypes.assertThatThrownBy(() -> getApplicantAndRespondent(asylumCase, testFunc -> AsylumCaseUtils.getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST)))
            .hasMessage("Correct respondent type is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void generateAppellantPin_return_existing_pin_if_present() {
        PinInPostDetails existingPin = PinInPostDetails.builder()
            .accessCode("123")
            .expiryDate(LocalDate.now().plusDays(30).toString())
            .pinUsed(YesOrNo.NO)
            .build();

        when(asylumCase.read(uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_PIN_IN_POST, PinInPostDetails.class))
            .thenReturn(Optional.of(existingPin));

        assertEquals(existingPin, generateAppellantPinIfNotPresent(asylumCase));
    }

    @Test
    void generateAppellantPin_generate_new_pin_if_not_present() {
        generatorMockedStatic.when(() -> AccessCodeGenerator.generateAccessCode())
            .thenReturn(generatedCode);

        PinInPostDetails generatedPinDetails = generateAppellantPinIfNotPresent(asylumCaseSpy);

        assertEquals(generatedCode, generatedPinDetails.getAccessCode());
        assertEquals(LocalDate.now().plusDays(30).toString(), generatedPinDetails.getExpiryDate());
        assertEquals(NO, generatedPinDetails.getPinUsed());
    }

    @Test
    void submissionOutOfTime_should_return_true() {
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(AsylumCaseUtils.isSubmissionOutOfTime(asylumCase));
    }

    @Test
    void submissionOutOfTime_should_return_false() {
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        assertFalse(AsylumCaseUtils.isSubmissionOutOfTime(asylumCase));
    }

    @Test
    void should_return_true_if_in_country_is_present() {
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertTrue(AsylumCaseUtils.hasAppellantAddressInCountryOrOutOfCountry(asylumCase));
    }

    @Test
    void should_return_true_if_ooc_is_present() {
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertTrue(AsylumCaseUtils.hasAppellantAddressInCountryOrOutOfCountry(asylumCase));
    }

    @Test
    void should_return_false_if_neither_in_country_nor_ooc_is_present() {
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J, YesOrNo.class)).thenReturn(Optional.empty());

        assertFalse(AsylumCaseUtils.hasAppellantAddressInCountryOrOutOfCountry(asylumCase));
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

    @ParameterizedTest
    @CsvSource({
        "14000, 8000, 60.00",
        "8000, 14000, 60.00",
        "10000, 10000, 0.00"
    })
    void should_return_absolute_fee_amount_even_when_negative_difference(String originalFeeTotal, String newFeeTotal, String expectedDifference) {
        String feeDifference = AsylumCaseUtils.calculateFeeDifference(originalFeeTotal, newFeeTotal);
        assertEquals(expectedDifference, feeDifference);
    }

}
