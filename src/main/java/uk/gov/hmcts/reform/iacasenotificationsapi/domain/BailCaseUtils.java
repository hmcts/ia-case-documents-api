package uk.gov.hmcts.reform.iacasenotificationsapi.domain;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.RECORD_DECISION_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RecordDecisionType.GRANTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RecordDecisionType.CONDITIONAL_GRANT;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RecordDecisionType.REFUSED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RecordDecisionType.REFUSED_UNDER_IMA;

import com.launchdarkly.shaded.com.google.common.base.Objects;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RecordDecisionType;

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
}
