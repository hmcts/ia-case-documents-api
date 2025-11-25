package uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils;


import org.apache.commons.lang3.tuple.ImmutablePair;
import org.assertj.core.api.AssertionsForClassTypes;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.api.mockito.PowerMockito;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;

import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ApplyForCosts;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumAppealType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.FtpaDecisionOutcomeType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.PinInPostDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AccessCodeGenerator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumAppealType.EA;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumAppealType.EU;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumAppealType.HU;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.ADDENDUM_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANTS_REPRESENTATION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPLIES_FOR_COSTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ARIA_MIGRATED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_EJP;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.OUT_OF_TIME_DECISION_DOCUMENT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.AMOUNT_REMITTED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_HAS_FIXED_ADDRESS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DIRECTION_EDIT_DATE_DUE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DIRECTION_EDIT_EXPLANATION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DIRECTION_EDIT_PARTIES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT_GBP;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_DECISION_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_REMOTE_HEARING;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_VIRTUAL_HEARING;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.REMISSION_DECISION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.RESPOND_TO_COSTS_LIST;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SUBMISSION_OUT_OF_TIME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionType.HO_WAIVER_REMISSION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionType.NO_REMISSION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;


@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class AsylumCaseUtilsTest {

    @Mock(lenient = true)
    private AsylumCase asylumCase;
    @Spy
    private AsylumCase asylumCaseSpy;
    @Mock
    private Document document;
    @Mock
    private AddressUk address;
    @Mock
    private DynamicList hearingChannelDynamicList;
    @Mock
    private Value hearingChannelValue;
    private final MockedStatic<AccessCodeGenerator> generatorMockedStatic = mockStatic(AccessCodeGenerator.class);
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

    @Test
    void shouldReturnTrue_whenRemissionDecisionIsApproved() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(RemissionDecision.APPROVED));

        boolean result = AsylumCaseUtils.isRemissionApproved(asylumCase);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalse_whenRemissionDecisionIsRejected() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(RemissionDecision.REJECTED));

        boolean result = AsylumCaseUtils.isRemissionApproved(asylumCase);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalse_whenRemissionDecisionIsEmpty() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.empty());

        boolean result = AsylumCaseUtils.isRemissionApproved(asylumCase);

        assertThat(result).isFalse();
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

    @Nested
    class GetDecisionOfNoticeDocuments {

        @Test
        void should_get_decision_of_notice_document_when_present() {
            when(asylumCase.read(OUT_OF_TIME_DECISION_DOCUMENT)).thenReturn(Optional.of(document));

            Optional<Document> result = AsylumCaseUtils.getDecisionOfNoticeDocuments(asylumCase);

            assertTrue(result.isPresent());
            assertEquals(document, result.get());
        }

        @Test
        void should_return_empty_optional_when_no_decision_of_notice_document_present() {
            when(asylumCase.read(OUT_OF_TIME_DECISION_DOCUMENT)).thenReturn(Optional.empty());

            Optional<Document> result = AsylumCaseUtils.getDecisionOfNoticeDocuments(asylumCase);

            assertFalse(result.isPresent());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"LEGAL_REPRESENTATIVE", "RESPONDENT", "APPELLANT", "BOTH", "APPELLANT_AND_RESPONDENT"})
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
    void should_return_due_date_plus_weeks() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_SUBMISSION_DATE, String.class)).thenReturn(Optional.of("2023-08-01"));

        String result = AsylumCaseUtils.dueDatePlusNumberOfWeeks(asylumCase, 2);

        // 2023-08-01 + 2 weeks = 2023-08-15
        assertThat(result).isEqualTo(DateUtils.formatDateForNotificationAttachmentDocument(LocalDate.of(2023, 8, 15)));
    }

    @Test
    void should_return_due_date_plus_days() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_SUBMISSION_DATE, String.class)).thenReturn(Optional.of("2023-08-01"));

        String result = AsylumCaseUtils.dueDatePlusNumberOfDays(asylumCase, 10);

        // 2023-08-01 + 10 days = 2023-08-11
        assertThat(result).isEqualTo(DateUtils.formatDateForNotificationAttachmentDocument(LocalDate.of(2023, 8, 11)));
    }

    @Test
    void should_throw_if_submission_date_missing_in_plus_weeks() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_SUBMISSION_DATE, String.class)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> AsylumCaseUtils.dueDatePlusNumberOfWeeks(asylumCase, 1));
    }

    @Test
    void should_throw_if_submission_date_missing_in_plus_days() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_SUBMISSION_DATE, String.class)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> AsylumCaseUtils.dueDatePlusNumberOfDays(asylumCase, 5));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class)
    void should_return_correct_value_for_hasAppealBeenSubmittedByAppellantInternalCase(YesOrNo yesOrNo) {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(yesOrNo));

        if (yesOrNo.equals(YES)) {
            assertTrue(AsylumCaseUtils.hasAppealBeenSubmittedByAppellantInternalCase(asylumCase));
        } else {
            assertFalse(AsylumCaseUtils.hasAppealBeenSubmittedByAppellantInternalCase(asylumCase));
        }
    }

    @Test
    void should_return_false_when_appellants_representation_is_not_present() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.empty());

        assertFalse(AsylumCaseUtils.hasAppealBeenSubmittedByAppellantInternalCase(asylumCase));
    }


    @Test
    void should_return_true_handle_has_been_submitted_internal_case() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YES));

        boolean result = AsylumCaseUtils.hasAppealBeenSubmittedByAppellantInternalCase(asylumCase);

        assertTrue(result);
    }

    @Test
    void should_return_false_handle_has_been_submitted_internal_case() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(NO));

        boolean result = AsylumCaseUtils.hasAppealBeenSubmittedByAppellantInternalCase(asylumCase);

        assertFalse(result);
    }

    @Test
    void should_return_false_handle_has_been_submitted_internal_case_if_missing_field() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.empty());

        boolean result = AsylumCaseUtils.hasAppealBeenSubmittedByAppellantInternalCase(asylumCase);

        assertFalse(result);
    }

    @Test
    void should_return_true_submission_out_of_time() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(AsylumCaseDefinition.SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YES));

        boolean result = AsylumCaseUtils.isSubmissionOutOfTime(asylumCase);

        assertTrue(result);
    }

    @Test
    void should_return_false_submission_out_of_time() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(AsylumCaseDefinition.SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));

        boolean result = AsylumCaseUtils.isSubmissionOutOfTime(asylumCase);

        assertFalse(result);
    }

    @Test
    void should_return_false_submission_out_of_time_if_missing_field() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.empty());

        boolean result = AsylumCaseUtils.isSubmissionOutOfTime(asylumCase);

        assertFalse(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"RP", "DC"})
    void should_return_true_for_fee_exempt_appeal_types(String appealTypeValue) {
        AsylumAppealType appealType = AsylumAppealType.valueOf(appealTypeValue);
        when(asylumCase.read(APPEAL_TYPE, AsylumAppealType.class)).thenReturn(Optional.of(appealType));

        assertTrue(isFeeExemptAppeal(asylumCase));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PA", "EA", "HU", "EU"})
    void should_return_false_for_non_fee_exempt_appeal_types(String appealTypeValue) {
        AsylumAppealType appealType = AsylumAppealType.valueOf(appealTypeValue);
        when(asylumCase.read(APPEAL_TYPE, AsylumAppealType.class)).thenReturn(Optional.of(appealType));

        assertFalse(isFeeExemptAppeal(asylumCase));
    }

    @Test
    void should_return_false_when_appeal_type_is_not_present() {
        when(asylumCase.read(APPEAL_TYPE, AsylumAppealType.class)).thenReturn(Optional.empty());

        assertFalse(isFeeExemptAppeal(asylumCase));
    }

    @Test
    void should_return_true_when_journey_type_is_rep() {
        AsylumCase asylumCase = Mockito.mock(AsylumCase.class);

        when(asylumCase.read(AsylumCaseDefinition.JOURNEY_TYPE, JourneyType.class))
                .thenReturn(Optional.of(JourneyType.REP));

        assertTrue(isRepJourney(asylumCase));
    }

    @Test
    void should_return_false_when_journey_type_is_not_rep() {
        AsylumCase asylumCase = Mockito.mock(AsylumCase.class);

        when(asylumCase.read(AsylumCaseDefinition.JOURNEY_TYPE, JourneyType.class))
                .thenReturn(Optional.of(JourneyType.AIP)); // e.g., Appellant in Person

        assertFalse(isRepJourney(asylumCase));
    }

    @Test
    void should_return_true_when_journey_type_is_absent() {
        AsylumCase asylumCase = Mockito.mock(AsylumCase.class);

        when(asylumCase.read(AsylumCaseDefinition.JOURNEY_TYPE, JourneyType.class))
                .thenReturn(Optional.empty());

        assertTrue(isRepJourney(asylumCase));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PARTIALLY_APPROVED", "REJECTED"})
    void should_return_true_for_remission_decision_partially_granted_or_refused(String remissionDecisionValue) {
        RemissionDecision remissionDecision = RemissionDecision.valueOf(remissionDecisionValue);
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));

        assertTrue(AsylumCaseUtils.remissionDecisionPartiallyGrantedOrRefused(asylumCase));
    }

    @ParameterizedTest
    @ValueSource(strings = {"APPROVED"})
    void should_return_false_for_other_remission_decisions(String remissionDecisionValue) {
        RemissionDecision remissionDecision = RemissionDecision.valueOf(remissionDecisionValue);
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));

        assertFalse(AsylumCaseUtils.remissionDecisionPartiallyGrantedOrRefused(asylumCase));
    }

    @Test
    void should_return_false_when_remission_decision_is_not_present() {
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.empty());

        assertFalse(AsylumCaseUtils.remissionDecisionPartiallyGrantedOrRefused(asylumCase));
    }

    @Test
    void should_return_true_for_remission_decision_partially_granted() {
        RemissionDecision remissionDecision = RemissionDecision.PARTIALLY_APPROVED;
        Mockito.when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));

        assertTrue(remissionDecisionPartiallyGranted(asylumCase));
    }

    @ParameterizedTest
    @ValueSource(strings = {"APPROVED", "REJECTED"})
    void should_return_false_for_remission_decision_not_partially_granted(String remissionDecisionValue) {
        RemissionDecision remissionDecision = RemissionDecision.valueOf(remissionDecisionValue);
        Mockito.when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));

        assertFalse(remissionDecisionPartiallyGranted(asylumCase));
    }

    @Test
    void should_return_false_when_remission_decision_is_not_present_for_partially_granted() {
        Mockito.when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.empty());

        assertFalse(remissionDecisionPartiallyGranted(asylumCase));
    }

    @Test
    void should_return_true_for_remission_decision_granted() {
        RemissionDecision remissionDecision = RemissionDecision.APPROVED;
        Mockito.when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));

        assertTrue(remissionDecisionGranted(asylumCase));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PARTIALLY_APPROVED", "REJECTED"})
    void should_return_false_for_remission_decision_not_approved(String remissionDecisionValue) {
        RemissionDecision remissionDecision = RemissionDecision.valueOf(remissionDecisionValue);
        Mockito.when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));

        assertFalse(remissionDecisionGranted(asylumCase));
    }

    @Test
    void should_return_hearing_channel_label_when_present() {
        String expectedLabel = "In person";
        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(hearingChannelDynamicList));
        when(hearingChannelDynamicList.getValue()).thenReturn(hearingChannelValue);
        when(hearingChannelValue.getLabel()).thenReturn(expectedLabel);

        String result = AsylumCaseUtils.getHearingChannel(asylumCase, "Unknown");

        assertEquals(expectedLabel, result);
    }

    @Test
    void should_return_default_value_when_hearing_channel_not_present() {
        String defaultValue = "Unknown";
        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.empty());

        String result = AsylumCaseUtils.getHearingChannel(asylumCase, defaultValue);

        assertEquals(defaultValue, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"In person", "Video call", "Telephone", "Paper hearing"})
    void should_return_correct_hearing_channel_for_different_types(String hearingChannelType) {
        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(hearingChannelDynamicList));
        when(hearingChannelDynamicList.getValue()).thenReturn(hearingChannelValue);
        when(hearingChannelValue.getLabel()).thenReturn(hearingChannelType);

        String result = AsylumCaseUtils.getHearingChannel(asylumCase, "Unknown");

        assertEquals(hearingChannelType, result);
    }

    @Test
    void should_throw_exception_when_hearing_channel_value_is_null() {
        String defaultValue = "Not specified";
        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(hearingChannelDynamicList));
        when(hearingChannelDynamicList.getValue()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> {
            AsylumCaseUtils.getHearingChannel(asylumCase, defaultValue);
        });
    }

    @Nested
    class AddIndefiniteArticleTest {

        @ParameterizedTest
        @CsvSource({
            "Admin, An Admin",
            "admin, An admin",
            "Officer, An Officer",
            "officer, An officer",
            "Employee, An Employee",
            "employee, An employee",
            "Individual, An Individual",
            "individual, An individual",
            "User, A User",
            "user, A user",
            "University, A University",
            "university, A university",
            "Legal Worker, A Legal Worker",
            "legal worker, A legal worker",
            "Appeal Officer, An Appeal Officer",
            "appeal officer, An appeal officer",
            "Honor, An Honor",
            "honor, An honor",
            "Hour, An Hour",
            "hour, An hour"
        })
        void should_add_correct_indefinite_article(String input, String expected) {
            String result = AsylumCaseUtils.addIndefiniteArticle(input);
            assertEquals(expected, result);
        }

        @Test
        void should_return_empty_string_for_null_input() {
            String result = AsylumCaseUtils.addIndefiniteArticle(null);
            assertEquals("", result);
        }

        @Test
        void should_return_empty_string_for_empty_input() {
            String result = AsylumCaseUtils.addIndefiniteArticle("");
            assertEquals("", result);
        }

        @Test
        void should_return_empty_string_for_whitespace_input() {
            String result = AsylumCaseUtils.addIndefiniteArticle("   ");
            assertEquals("", result);
        }

        @Test
        void should_handle_single_character_inputs() {
            assertEquals("A B", AsylumCaseUtils.addIndefiniteArticle("B"));
            assertEquals("An A", AsylumCaseUtils.addIndefiniteArticle("A"));
            assertEquals("An E", AsylumCaseUtils.addIndefiniteArticle("E"));
            assertEquals("An I", AsylumCaseUtils.addIndefiniteArticle("I"));
            assertEquals("An O", AsylumCaseUtils.addIndefiniteArticle("O"));
            assertEquals("An U", AsylumCaseUtils.addIndefiniteArticle("U"));
        }

        @Test
        void should_trim_input_before_processing() {
            String result = AsylumCaseUtils.addIndefiniteArticle("  Admin  ");
            assertEquals("An Admin", result);
        }
    }

    @Test
    void should_return_correct_value_for_det() {
        PowerMockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(isAppellantInDetention(asylumCase));
    }

    @Test
    void should_return_correct_value_for_ada() {
        PowerMockito.when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(isAcceleratedDetainedAppeal(asylumCase));
    }

    @Test
    void should_return_correct_value_for_aaa() {
        PowerMockito.when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.AG));
        assertTrue(isAgeAssessmentAppeal(asylumCase));
    }

    @Test
    void isAdmin_should_return_true() {
        PowerMockito.when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(isInternalCase(asylumCase));
    }

    @Test
    void isAdmin_should_return_false() {
        PowerMockito.when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(NO));
        assertFalse(isInternalCase(asylumCase));
    }

    @Test
    void isNotInternalOrIsInternalWithLegalRepresentation_should_return_true() {
        PowerMockito.when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        PowerMockito.when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertTrue(isNotInternalOrIsInternalWithLegalRepresentation(asylumCase));
    }

    @Test
    void isNotInternalOrIsInternalWithLegalRepresentation_should_return_false() {
        PowerMockito.when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));

        assertFalse(isNotInternalOrIsInternalWithLegalRepresentation(asylumCase));
    }

    @Test
    void isAriaMigrated_should_return_true() {
        PowerMockito.when(asylumCase.read(IS_ARIA_MIGRATED, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(isAriaMigrated(asylumCase));
    }

    @Test
    void isAriaMigrated_should_return_false() {
        PowerMockito.when(asylumCase.read(IS_ARIA_MIGRATED, YesOrNo.class)).thenReturn(Optional.of(NO));
        assertFalse(isAriaMigrated(asylumCase));
    }

    @Test
    void isAipJourney_should_return_true() {
        PowerMockito.when(asylumCase.read(JOURNEY_TYPE, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.JourneyType.class)).thenReturn(Optional.of(
            uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.JourneyType.AIP));
        assertTrue(isAipJourney(asylumCase));
    }

    @Test
    void isAipJourney_should_return_false() {
        PowerMockito.when(asylumCase.read(JOURNEY_TYPE, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.JourneyType.class)).thenReturn(Optional.of(
            uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.JourneyType.REP));
        assertFalse(isAipJourney(asylumCase));
    }

    @Test
    void getFtpaDecisionOutcomeType_should_return_granted() {
        PowerMockito.when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_GRANTED));
        assertEquals(FtpaDecisionOutcomeType.FTPA_GRANTED, getFtpaDecisionOutcomeType(asylumCase).orElse(null));
    }

    @Test
    void getFtpaDecisionOutcomeType_should_return_refused() {
        PowerMockito.when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_REFUSED));
        PowerMockito.when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.empty());
        assertEquals(FtpaDecisionOutcomeType.FTPA_REFUSED, getFtpaDecisionOutcomeType(asylumCase).orElse(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"birmingham", "", "harmondsworth"})
    void isListed_should_return_correct_value(String hearingCentre) {
        Optional<HearingCentre> mayBeListCaseHearingCenter = HearingCentre.from(hearingCentre);
        PowerMockito.when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(mayBeListCaseHearingCenter);
        assertEquals(mayBeListCaseHearingCenter.isPresent(), isAppealListed(asylumCase));
    }

    @Test
    void should_get_addendum_document_when_present() {
        List<IdValue<DocumentWithMetadata>> addendumDocuments = new ArrayList<>();
        addendumDocuments.add(addendumOne);
        PowerMockito.when(asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(addendumDocuments));

        assertEquals(addendumDocuments, getAddendumEvidenceDocuments(asylumCase));
        assertEquals(Optional.of(addendumOne), getLatestAddendumEvidenceDocument(asylumCase));
    }

    @Test
    void should_get_addendum_documents_when_more_than_one_exists() {
        List<IdValue<DocumentWithMetadata>> addendumDocuments = new ArrayList<>();
        addendumDocuments.add(addendumOne);
        addendumDocuments.add(addendumTwo);
        PowerMockito.when(asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(addendumDocuments));

        assertEquals(addendumDocuments, getAddendumEvidenceDocuments(asylumCase));
        assertEquals(2, getAddendumEvidenceDocuments(asylumCase).size());
    }

    @Test
    void should_return_empty_list_when_no_addendum_evidence_documents_present() {
        PowerMockito.when(asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS)).thenReturn(Optional.empty());

        assertEquals(Collections.emptyList(), getAddendumEvidenceDocuments(asylumCase));
        assertEquals(Optional.empty(), getLatestAddendumEvidenceDocument(asylumCase));
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
        PowerMockito.when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.empty());
        AssertionsForClassTypes.assertThatThrownBy(() -> retrieveLatestApplyForCosts(asylumCase))
            .hasMessage("Applies for costs are not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_retrieve_latest_created_apply_for_costs() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Wasted costs", "Home office", "Legal representative", applyForCostsCreationDate)),
            new IdValue<>("1", new ApplyForCosts("Unreasonable costs", "Legal representative", "Home office", applyForCostsCreationDate))
        );
        PowerMockito.when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        assertEquals(applyForCostsList.get(0).getValue(), retrieveLatestApplyForCosts(asylumCase));
    }

    @Test
    void should_retrieve_application_by_id() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Wasted costs", "Home office", "Legal representative", applyForCostsCreationDate)),
            new IdValue<>("1", new ApplyForCosts("Unreasonable costs", "Legal representative", "Home office", applyForCostsCreationDate))
        );
        DynamicList respondsToCostsList = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        PowerMockito.when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(respondsToCostsList));
        PowerMockito.when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        assertEquals(applyForCostsList.get(0).getValue(), getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST));
    }

    @Test
    void should_throw_if_applies_are_not_present() {
        DynamicList respondsToCostsList = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        PowerMockito.when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(respondsToCostsList));
        PowerMockito.when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.empty());

        AssertionsForClassTypes.assertThatThrownBy(() -> getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST))
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

        PowerMockito.when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(respondsToCostsList));
        PowerMockito.when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        AssertionsForClassTypes.assertThatThrownBy(() -> getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST))
            .hasMessage("Apply for costs with id 3 not found")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_check_if_logged_user_is_home_office() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Home office", "Wasted costs"))
        );
        DynamicList respondsToCostsList = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        PowerMockito.when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(respondsToCostsList));
        PowerMockito.when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        assertTrue(isLoggedUserIsHomeOffice(asylumCase, testFunc -> getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST)));
    }

    @Test
    void should_throw_if_logged_user_is__of_incorrect_type() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Tribunal", "Wasted costs"))
        );
        DynamicList respondsToCostsList = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        PowerMockito.when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(respondsToCostsList));
        PowerMockito.when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        AssertionsForClassTypes.assertThatThrownBy(() -> isLoggedUserIsHomeOffice(asylumCase, testFunc -> getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST)))
            .hasMessage("Correct applicant type is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_build_proper_pair_with_applicant_and_respondent() {
        DynamicList selectedValue = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Wasted costs", "Legal representative", "Home office", applyForCostsCreationDate))
        );

        PowerMockito.when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(selectedValue));
        PowerMockito.when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        ImmutablePair<String, String> getApplicantAndRespondent = getApplicantAndRespondent(asylumCase, testFunc -> getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST));

        assertEquals("Legal representative", getApplicantAndRespondent.getRight());
        assertEquals("Home office", getApplicantAndRespondent.getLeft());
    }

    @Test
    void should_throw_if_applicant_type_is_not_correct() {
        DynamicList selectedValue = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Wasted costs", "Tribunal", "Case officer", applyForCostsCreationDate))
        );

        PowerMockito.when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(selectedValue));
        PowerMockito.when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        AssertionsForClassTypes.assertThatThrownBy(() -> getApplicantAndRespondent(asylumCase, testFunc -> getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST)))
            .hasMessage("Correct applicant type is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_if_respondent_type_is_not_correct() {
        DynamicList selectedValue = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Wasted costs", "Case officer", "Tribunal", applyForCostsCreationDate))
        );

        PowerMockito.when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(selectedValue));
        PowerMockito.when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        AssertionsForClassTypes.assertThatThrownBy(() -> getApplicantAndRespondent(asylumCase, testFunc -> getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST)))
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

        PowerMockito.when(asylumCase.read(AsylumCaseDefinition.APPELLANT_PIN_IN_POST, PinInPostDetails.class))
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
        PowerMockito.when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(isSubmissionOutOfTime(asylumCase));
    }

    @Test
    void submissionOutOfTime_should_return_false() {
        PowerMockito.when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        assertFalse(isSubmissionOutOfTime(asylumCase));
    }

    @Test
    void should_return_true_if_in_country_is_present() {
        PowerMockito.when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertTrue(hasAppellantAddressInCountryOrOutOfCountry(asylumCase));
    }

    @Test
    void should_return_true_if_ooc_is_present() {
        PowerMockito.when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.empty());
        PowerMockito.when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertTrue(hasAppellantAddressInCountryOrOutOfCountry(asylumCase));
    }

    @Test
    void should_return_false_if_neither_in_country_nor_ooc_is_present() {
        PowerMockito.when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.empty());
        PowerMockito.when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J, YesOrNo.class)).thenReturn(Optional.empty());
        PowerMockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertFalse(hasAppellantAddressInCountryOrOutOfCountry(asylumCase));
    }

    @Test
    void should_return_true_if_appellant_is_detained_in_other_facility() {
        PowerMockito.when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.empty());
        PowerMockito.when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J, YesOrNo.class)).thenReturn(Optional.empty());
        PowerMockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        PowerMockito.when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));

        assertTrue(hasAppellantAddressInCountryOrOutOfCountry(asylumCase));
    }

    @Test
    void should_return_false_if_appellant_is_detained_in_non_other_facility() {
        PowerMockito.when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.empty());
        PowerMockito.when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J, YesOrNo.class)).thenReturn(Optional.empty());
        PowerMockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        PowerMockito.when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));

        assertFalse(hasAppellantAddressInCountryOrOutOfCountry(asylumCase));
    }

    @ParameterizedTest
    @CsvSource({
        "14000, 8000, 60.00",
        "8000, 14000, 60.00",
        "10000, 10000, 0.00"
    })
    void should_return_absolute_fee_amount_even_when_negative_difference(String originalFeeTotal, String newFeeTotal, String expectedDifference) {
        String feeDifference = calculateFeeDifference(originalFeeTotal, newFeeTotal);
        assertEquals(expectedDifference, feeDifference);
    }

    @Test
    void should_return_true_when_appellant_is_in_detention_and_one_of_facility_types_matches() {
        Mockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        Mockito.when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));

        assertTrue(isDetainedInOneOfFacilityTypes(asylumCase, DetentionFacility.IRC));
        assertTrue(isDetainedInOneOfFacilityTypes(asylumCase, DetentionFacility.IRC, DetentionFacility.PRISON));
    }

    @Test
    void should_return_false_when_appellant_is_in_detention_and_none_of_facility_types_matches() {
        Mockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        Mockito.when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));

        assertFalse(isDetainedInOneOfFacilityTypes(asylumCase, DetentionFacility.IRC));
        assertFalse(isDetainedInOneOfFacilityTypes(asylumCase, DetentionFacility.IRC, DetentionFacility.PRISON));
    }

    @Test
    void should_return_false_when_appellant_is_not_in_detention_for_multipole_facility_types() {
        Mockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertFalse(isDetainedInOneOfFacilityTypes(asylumCase, DetentionFacility.IRC));
        assertFalse(isDetainedInOneOfFacilityTypes(asylumCase, DetentionFacility.IRC, DetentionFacility.PRISON));
    }

    @Test
    void should_return_true_when_appellant_is_in_detention_and_facility_type_matches() {
        PowerMockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        PowerMockito.when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));

        assertTrue(isDetainedInFacilityType(asylumCase, DetentionFacility.IRC));
    }

    @Test
    void should_return_false_when_appellant_is_not_in_detention() {
        PowerMockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertFalse(isDetainedInFacilityType(asylumCase, DetentionFacility.IRC));
    }

    @Test
    void should_return_false_when_facility_type_does_not_match() {
        PowerMockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        PowerMockito.when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));

        assertFalse(isDetainedInFacilityType(asylumCase, DetentionFacility.IRC));
    }

    @Test
    void should_return_false_when_detention_facility_is_empty() {
        PowerMockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        PowerMockito.when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());

        assertFalse(isDetainedInFacilityType(asylumCase, DetentionFacility.IRC));
    }

    @ParameterizedTest
    @CsvSource({
        "immigrationRemovalCentre, IRC",
        "prison, PRISON",
        "other, OTHER"
    })
    void should_return_true_for_all_facility_types_when_appellant_is_detained(String detentionFacilityValue, DetentionFacility facilityType) {
        PowerMockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        PowerMockito.when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(detentionFacilityValue));

        assertTrue(isDetainedInFacilityType(asylumCase, facilityType));
    }

    @Test
    void should_return_true_when_appellant_is_detained_in_any_of_the_specified_facility_types() {
        PowerMockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        PowerMockito.when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));

        assertTrue(
            isDetainedInOneOfFacilityTypes(asylumCase, DetentionFacility.IRC, DetentionFacility.PRISON, DetentionFacility.OTHER));
    }

    @Test
    void should_return_true_when_appellant_is_detained_in_first_specified_facility_type() {
        PowerMockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        PowerMockito.when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));

        assertTrue(
            isDetainedInOneOfFacilityTypes(asylumCase, DetentionFacility.IRC, DetentionFacility.PRISON));
    }

    @Test
    void should_return_false_when_appellant_is_detained_in_none_of_the_specified_facility_types() {
        PowerMockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        PowerMockito.when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));

        assertFalse(
            isDetainedInOneOfFacilityTypes(asylumCase, DetentionFacility.IRC, DetentionFacility.PRISON));
    }

    @Test
    void should_return_false_when_appellant_is_not_detained_for_facility_types_check() {
        PowerMockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertFalse(
            isDetainedInOneOfFacilityTypes(asylumCase, DetentionFacility.IRC, DetentionFacility.PRISON, DetentionFacility.OTHER));
    }

    @Test
    void should_return_false_when_no_facility_types_specified() {
        assertFalse(isDetainedInOneOfFacilityTypes(asylumCase));
    }

    @ParameterizedTest
    @ValueSource(strings = {"RP", "DC"})
    void should_return_true_for_fee_exempt_appeal_types(String appealTypeValue) {
        AppealType appealType = AppealType.valueOf(appealTypeValue);
        PowerMockito.when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        assertTrue(isFeeExemptAppeal(asylumCase));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PA", "EA", "HU", "EU", "AG"})
    void should_return_false_for_non_fee_exempt_appeal_types(String appealTypeValue) {
        AppealType appealType = AppealType.valueOf(appealTypeValue);
        PowerMockito.when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        assertFalse(isFeeExemptAppeal(asylumCase));
    }

    @Test
    void should_return_false_when_appeal_type_is_not_present() {
        PowerMockito.when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.empty());

        assertFalse(isFeeExemptAppeal(asylumCase));
    }

    @Test
    void shouldReturnTrue_whenRemissionDecisionIsApproved() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        Mockito.when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(RemissionDecision.APPROVED));

        boolean result = AsylumCaseUtils.isRemissionApproved(asylumCase);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalse_whenRemissionDecisionIsRejected() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        Mockito.when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(RemissionDecision.REJECTED));

        boolean result = AsylumCaseUtils.isRemissionApproved(asylumCase);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalse_whenRemissionDecisionIsEmpty() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        Mockito.when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.empty());

        boolean result = AsylumCaseUtils.isRemissionApproved(asylumCase);

        assertThat(result).isFalse();
    }

    @Test
    void should_return_true_when_is_hearing_channel() {
        DynamicList hearingChannelList = new DynamicList(
            new Value("INTER", "In Person"),
            List.of(new Value("INTER", "In Person"),
                    new Value("NA", "Not in Attendance"),
                    new Value("VID", "Video"),
                    new Value("TEL", "Telephone"))
        );

        PowerMockito.when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(hearingChannelList));

        assertTrue(isHearingChannel(asylumCase, "INTER"));
    }

    @Test
    void should_return_false_when_not_hearing_channel() {
        DynamicList hearingChannelList = new DynamicList(
            new Value("VID", "Video"),
            List.of(new Value("INTER", "In Person"),
                    new Value("NA", "Not in Attendance"),
                    new Value("VID", "Video"),
                    new Value("TEL", "Telephone"))
        );

        PowerMockito.when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(hearingChannelList));

        assertFalse(isHearingChannel(asylumCase, "INTER"));
    }

    @Test
    void should_return_false_when_hearing_channel_is_empty() {
        PowerMockito.when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.empty());

        assertFalse(isHearingChannel(asylumCase, "INTER"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PARTIALLY_APPROVED", "REJECTED"})
    void should_return_true_for_remission_decision_partially_granted_or_refused(String remissionDecisionValue) {
        RemissionDecision remissionDecision = RemissionDecision.valueOf(remissionDecisionValue);
        Mockito.when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));

        assertTrue(remissionDecisionPartiallyGrantedOrRefused(asylumCase));
    }

    @ParameterizedTest
    @ValueSource(strings = {"APPROVED"})
    void should_return_false_for_other_remission_decisions(String remissionDecisionValue) {
        RemissionDecision remissionDecision = RemissionDecision.valueOf(remissionDecisionValue);
        Mockito.when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));

        assertFalse(remissionDecisionPartiallyGrantedOrRefused(asylumCase));
    }

    @Test
    void should_return_false_when_remission_decision_is_not_present() {
        Mockito.when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.empty());

        assertFalse(remissionDecisionPartiallyGrantedOrRefused(asylumCase));
    }

    @Test
    void should_return_true_for_remission_decision_partially_granted() {
        RemissionDecision remissionDecision = RemissionDecision.PARTIALLY_APPROVED;
        Mockito.when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));

        assertTrue(remissionDecisionPartiallyGranted(asylumCase));
    }

    @ParameterizedTest
    @ValueSource(strings = {"APPROVED", "REJECTED"})
    void should_return_false_for_remission_decision_not_partially_granted(String remissionDecisionValue) {
        RemissionDecision remissionDecision = RemissionDecision.valueOf(remissionDecisionValue);
        Mockito.when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));

        assertFalse(remissionDecisionPartiallyGranted(asylumCase));
    }

    @Test
    void should_return_false_when_remission_decision_is_not_present_for_partially_granted() {
        Mockito.when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.empty());

        assertFalse(remissionDecisionPartiallyGranted(asylumCase));
    }

    @Test
    void should_return_true_for_internal_non_detained_case() {
        Mockito.when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        Mockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertTrue(AsylumCaseUtils.isInternalNonDetainedCase(asylumCase));
    }

    @ParameterizedTest
    @CsvSource({
        "NO, NO, false",
        "YES, YES, false",
        "YES, NO, true"
    })
    void should_return_correct_value_for_internal_non_detained_case(YesOrNo isAdmin, YesOrNo inDetention, boolean expected) {
        Mockito.when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(isAdmin));
        Mockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(inDetention));

        assertEquals(expected, isInternalNonDetainedCase(asylumCase));
    }

    @Test
    void should_return_true_for_internal_non_detained_with_in_country_address() {
        Mockito.when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        Mockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));
        Mockito.when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YES));
        Mockito.when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J, YesOrNo.class)).thenReturn(Optional.empty());
        Mockito.when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());

        assertTrue(internalNonDetainedWithAddressAvailable(asylumCase));
    }

    @Test
    void should_return_true_for_internal_non_detained_with_out_of_country_address() {
        Mockito.when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        Mockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));
        Mockito.when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.empty());
        Mockito.when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J, YesOrNo.class)).thenReturn(Optional.of(YES));
        Mockito.when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());

        assertTrue(internalNonDetainedWithAddressAvailable(asylumCase));
    }

    @Test
    void should_return_false_for_internal_non_detained_without_address() {
        Mockito.when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        Mockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));
        Mockito.when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.empty());
        Mockito.when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J, YesOrNo.class)).thenReturn(Optional.empty());
        Mockito.when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());

        assertFalse(internalNonDetainedWithAddressAvailable(asylumCase));
    }

    @Test
    void should_return_false_for_non_internal_case_with_address() {
        Mockito.when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(NO));
        Mockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));
        Mockito.when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YES));
        Mockito.when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J, YesOrNo.class)).thenReturn(Optional.empty());
        Mockito.when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());

        assertFalse(internalNonDetainedWithAddressAvailable(asylumCase));
    }

    @Test
    void should_return_false_for_internal_detained_case_with_address() {
        Mockito.when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        Mockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        Mockito.when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YES));
        Mockito.when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J, YesOrNo.class)).thenReturn(Optional.empty());
        Mockito.when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));

        assertFalse(internalNonDetainedWithAddressAvailable(asylumCase));
    }

    @Test
    void should_return_true_for_remission_decision_granted() {
        RemissionDecision remissionDecision = RemissionDecision.APPROVED;
        Mockito.when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));

        assertTrue(remissionDecisionGranted(asylumCase));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PARTIALLY_APPROVED", "REJECTED"})
    void should_return_false_for_remission_decision_not_approved(String remissionDecisionValue) {
        RemissionDecision remissionDecision = RemissionDecision.valueOf(remissionDecisionValue);
        Mockito.when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));

        assertFalse(remissionDecisionGranted(asylumCase));
    }

    @Test
    @SuppressWarnings("unchecked")
    @MockitoSettings(strictness = Strictness.LENIENT)
    void should_return_true_when_hearing_centre_updated() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        CaseDetails<AsylumCase> caseDetailsBefore = Mockito.mock(CaseDetails.class);
        AsylumCase asylumCaseBefore = mock(AsylumCase.class);

        Mockito.when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        setupHearingDataNotUpdated(asylumCase, asylumCaseBefore);

        Mockito.when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.MANCHESTER));

        assertTrue(AsylumCaseUtils.isHearingDetailsUpdated(asylumCase, Optional.of(caseDetailsBefore)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_return_false_when_hearing_details_not_updated() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        CaseDetails<AsylumCase> caseDetailsBefore = mock(CaseDetails.class);
        AsylumCase asylumCaseBefore = mock(AsylumCase.class);

        Mockito.when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        setupHearingDataNotUpdated(asylumCase, asylumCaseBefore);

        assertFalse(AsylumCaseUtils.isHearingDetailsUpdated(asylumCase, Optional.of(caseDetailsBefore)));
    }

    @Test
    void should_return_false_when_case_details_before_empty() {
        AsylumCase asylumCase = mock(AsylumCase.class);

        assertFalse(AsylumCaseUtils.isHearingDetailsUpdated(asylumCase, Optional.empty()));
    }

    @Test
    @SuppressWarnings("unchecked")
    @MockitoSettings(strictness = Strictness.LENIENT)
    void should_return_true_when_hearing_date_updated() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        CaseDetails<AsylumCase> caseDetailsBefore = mock(CaseDetails.class);
        AsylumCase asylumCaseBefore = mock(AsylumCase.class);

        Mockito.when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        setupHearingDataNotUpdated(asylumCase, asylumCaseBefore);

        Mockito.when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of("2023-10-02"));

        assertTrue(AsylumCaseUtils.isHearingDetailsUpdated(asylumCase, Optional.of(caseDetailsBefore)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_return_true_when_hearing_channel_updated() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        CaseDetails<AsylumCase> caseDetailsBefore = mock(CaseDetails.class);
        AsylumCase asylumCaseBefore = mock(AsylumCase.class);

        Mockito.when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        setupHearingDataNotUpdated(asylumCase, asylumCaseBefore);

        Mockito.when(asylumCase.read(AsylumCaseDefinition.HEARING_CHANNEL, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList(new Value("telephone", "Telephone"), List.of(new Value("telephone", "Telephone")))));

        assertTrue(AsylumCaseUtils.isHearingDetailsUpdated(asylumCase, Optional.of(caseDetailsBefore)));
    }

    @Test
    @SuppressWarnings("unchecked")
    @MockitoSettings(strictness = Strictness.LENIENT)
    void should_return_true_when_all_updated() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        CaseDetails<AsylumCase> caseDetailsBefore = mock(CaseDetails.class);
        AsylumCase asylumCaseBefore = mock(AsylumCase.class);

        Mockito.when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        setupHearingDataNotUpdated(asylumCase, asylumCaseBefore);

        Mockito.when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.MANCHESTER));

        Mockito.when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of("2023-10-02"));

        Mockito.when(asylumCase.read(AsylumCaseDefinition.HEARING_CHANNEL, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList(new Value("telephone", "Telephone"), List.of(new Value("telephone", "Telephone")))));

        assertTrue(AsylumCaseUtils.isHearingDetailsUpdated(asylumCase, Optional.of(caseDetailsBefore)));
    }


    void setupHearingDataNotUpdated(AsylumCase asylumCase, AsylumCase asylumCaseBefore) {

        Mockito.when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        Mockito.when(asylumCaseBefore.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

        Mockito.when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of("2023-10-01"));
        Mockito.when(asylumCaseBefore.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of("2023-10-01"));

        Mockito.when(asylumCase.read(AsylumCaseDefinition.HEARING_CHANNEL, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList(new Value("video", "Video"), List.of(new Value("video", "Video")))));
        Mockito.when(asylumCaseBefore.read(AsylumCaseDefinition.HEARING_CHANNEL, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList(new Value("video", "Video"), List.of(new Value("video", "Video")))));
    }

}
