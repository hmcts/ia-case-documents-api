package uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumAppealType.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionType.NO_REMISSION;
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
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
public class AsylumCaseUtilsTest {

    @Mock
    private AsylumCase asylumCase;
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
    void should_throw_for_amount_remitted_not_present() {
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        assertThatThrownBy(() -> AsylumCaseUtils.getFeeRemission(asylumCase))
                .isExactlyInstanceOf(RequiredFieldMissingException.class)
                .hasMessage("Amount remitted not found");
    }
}

