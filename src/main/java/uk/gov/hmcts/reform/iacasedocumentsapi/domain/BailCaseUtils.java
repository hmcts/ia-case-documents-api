package uk.gov.hmcts.reform.iacasedocumentsapi.domain;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_ALL_USERS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.RECORD_DECISION_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RecordDecisionType.GRANTED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RecordDecisionType.CONDITIONAL_GRANT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RecordDecisionType.REFUSED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RecordDecisionType.REFUSED_UNDER_IMA;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State.DECISION_CONDITIONAL_BAIL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;

import com.launchdarkly.shaded.com.google.common.base.Objects;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RecordDecisionType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;

public class BailCaseUtils {

    private BailCaseUtils() {
        // private constructor to prevent sonar warning
    }

    public static Boolean isBailGranted(BailCase bailCase) {
        RecordDecisionType recordDecisionType = bailCase.read(RECORD_DECISION_TYPE, RecordDecisionType.class)
            .orElse(null);
        if (Objects.equal(GRANTED, recordDecisionType)
            || Objects.equal(CONDITIONAL_GRANT, recordDecisionType)) {

            return true;
        }
        if (Objects.equal(REFUSED, recordDecisionType)
            || Objects.equal(REFUSED_UNDER_IMA, recordDecisionType)) {

            return false;
        }

        return null;
    }

    public static Boolean isBailConditionalGrant(BailCase bailCase) {
        String caseState = bailCase.read(CURRENT_CASE_STATE_VISIBLE_TO_ALL_USERS, String.class)
            .orElse(null);
        return Objects.equal(DECISION_CONDITIONAL_BAIL.toString(), caseState);
    }

    public static boolean isInternalCase(BailCase bailCase) {
        return bailCase.read(IS_ADMIN, YesOrNo.class).map(isAdmin -> YES == isAdmin).orElse(false);
    }
}
