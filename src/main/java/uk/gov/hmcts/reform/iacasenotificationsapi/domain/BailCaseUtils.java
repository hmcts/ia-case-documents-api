package uk.gov.hmcts.reform.iacasenotificationsapi.domain;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.RECORD_DECISION_TYPE;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;

public class BailCaseUtils {

    private BailCaseUtils() {
        // private constructor to prevent sonar warning
    }


    public static Boolean isBailGranted(BailCase bailCase) {
        if (bailCase.read(RECORD_DECISION_TYPE, String.class).orElse("").equals("granted")
            || bailCase.read(RECORD_DECISION_TYPE, String.class).orElse("").equals("conditionalGrant")) {
            return true;
        }
        if (bailCase.read(RECORD_DECISION_TYPE, String.class).orElse("").equals("refused")
            || bailCase.read(RECORD_DECISION_TYPE, String.class).orElse("").equals("refusedUnderIma")) {
            return false;
        }
        return null;
    }
}
