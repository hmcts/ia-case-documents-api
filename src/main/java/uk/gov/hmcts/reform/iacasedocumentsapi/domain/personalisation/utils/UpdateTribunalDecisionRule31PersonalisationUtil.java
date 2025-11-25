package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.TYPES_OF_UPDATE_TRIBUNAL_DECISION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.UPDATED_APPEAL_DECISION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;

public interface UpdateTribunalDecisionRule31PersonalisationUtil {

    default boolean isUpdatedDecision(AsylumCase asylumCase) {

        DynamicList decision = asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class)
                .orElseThrow(() -> new IllegalStateException("typesOfUpdateTribunalDecision is not present"));
        return decision.getValue().getLabel().contains("Yes");
    }

    default boolean isUpdatedDocument(AsylumCase asylumCase) {
        YesOrNo isUpdatedDocument = asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class)
                .orElseThrow(() -> new IllegalStateException("updateTribunalDecisionAndReasonsFinalCheck is not present"));
        return isUpdatedDocument.equals(YesOrNo.YES);
    }

    default void buildUpdatedDecisionData(AsylumCase asylumCase, ImmutableMap.Builder<String, String> personalizationBuilder) {
        if (isUpdatedDecision(asylumCase)) {
            YesOrNo appealOutOfCountry = asylumCase.read(AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY, YesOrNo.class)
                    .orElse(YesOrNo.NO);
            String period = appealOutOfCountry == YesOrNo.YES ? "28 days" : "14 days";
            String updatedAppealDecision = asylumCase.read(UPDATED_APPEAL_DECISION, String.class)
                    .orElseThrow(() -> new IllegalStateException("updatedAppealDecision is not present"));

            personalizationBuilder.put("oldDecision", updatedAppealDecision.equals("Allowed") ? "Dismissed" : "Allowed");
            personalizationBuilder.put("newDecision", updatedAppealDecision);
            personalizationBuilder.put("period", period);
        }
    }
}
