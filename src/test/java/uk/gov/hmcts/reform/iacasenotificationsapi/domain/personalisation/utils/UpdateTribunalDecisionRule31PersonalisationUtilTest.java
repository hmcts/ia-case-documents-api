package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.TYPES_OF_UPDATE_TRIBUNAL_DECISION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.UPDATED_APPEAL_DECISION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK;

import java.util.Optional;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
public class UpdateTribunalDecisionRule31PersonalisationUtilTest {
    @Mock
    AsylumCase asylumCase;
    @Mock
    ImmutableMap.Builder<String, String> personalizationBuilder;
    private final DynamicList dynamicAllowedDecisionList = new DynamicList(
            new Value("allowed", "Yes, change decision to Allowed"),
            newArrayList()
    );
    private final DynamicList dynamicDismissedDecisionList = new DynamicList(
            new Value("dismissed", "No"),
            newArrayList()
    );
    private final String days28 = "28 days";
    private final String days14 = "14 days";
    private UpdateTribunalDecisionRule31PersonalisationUtil updateTribunalDecisionRule31PersonalisationUtil;

    @BeforeEach
    public void setup() {
        updateTribunalDecisionRule31PersonalisationUtil = new UpdateTribunalDecisionRule31PersonalisationUtil() {};
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    void should_return_correct_is_document_updated_value(YesOrNo isUpdated) {
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class))
                .thenReturn(Optional.of(isUpdated));
        boolean resultIsUpdatedDecision = updateTribunalDecisionRule31PersonalisationUtil.isUpdatedDocument(asylumCase);
        if (isUpdated.equals(YesOrNo.YES)) {
            assertTrue(resultIsUpdatedDecision);
        } else {
            assertFalse(resultIsUpdatedDecision);
        }
    }

    @Test
    void should_return_true_if_is_decision_updated_value() {
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
                .thenReturn(Optional.of(dynamicAllowedDecisionList));
        boolean resultIsUpdatedDecision = updateTribunalDecisionRule31PersonalisationUtil.isUpdatedDecision(asylumCase);
        assertTrue(resultIsUpdatedDecision);
    }

    @Test
    void should_return_false_if_is_decision_updated_value() {
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
                .thenReturn(Optional.of(dynamicDismissedDecisionList));
        boolean resultIsUpdatedDecision = updateTribunalDecisionRule31PersonalisationUtil.isUpdatedDecision(asylumCase);
        assertFalse(resultIsUpdatedDecision);
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    void should_build_updated_decision(YesOrNo outOfCountry) {
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
                .thenReturn(Optional.of(dynamicAllowedDecisionList));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY, YesOrNo.class))
                .thenReturn(Optional.of(outOfCountry));
        when(asylumCase.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.of("Allowed"));
        updateTribunalDecisionRule31PersonalisationUtil.buildUpdatedDecisionData(asylumCase, personalizationBuilder);

        verify(personalizationBuilder, times(1)).put("oldDecision", "Dismissed");
        verify(personalizationBuilder, times(1)).put("newDecision", "Allowed");
        if (outOfCountry.equals(YesOrNo.YES)) {
            verify(personalizationBuilder, times(1)).put("period", days28);
        } else {
            verify(personalizationBuilder, times(1)).put("period", days14);
        }
    }

    @Test
    public void should_throw_error_if_types_of_updated_tribunal_decision_missing() {
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> updateTribunalDecisionRule31PersonalisationUtil.isUpdatedDecision(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("typesOfUpdateTribunalDecision is not present");
    }

    @Test
    public void should_throw_error_if_document_check_missing() {
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> updateTribunalDecisionRule31PersonalisationUtil.isUpdatedDocument(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("updateTribunalDecisionAndReasonsFinalCheck is not present");
    }

    @Test
    public void should_throw_error_if_updated_appeal_decision_missing() {
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
                .thenReturn(Optional.of(dynamicAllowedDecisionList));
        when(asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> updateTribunalDecisionRule31PersonalisationUtil.buildUpdatedDecisionData(asylumCase, personalizationBuilder))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("updatedAppealDecision is not present");
    }
}
