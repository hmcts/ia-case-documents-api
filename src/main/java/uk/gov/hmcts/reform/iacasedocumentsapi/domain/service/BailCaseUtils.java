package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;

public final class BailCaseUtils {

    private BailCaseUtils() {
        // private constructor to prevent sonar warning
    }
    
    public static boolean isImaEnabled(BailCase bailCase) {
        return bailCase.read(BailCaseFieldDefinition.IS_IMA_ENABLED, YesOrNo.class).orElse(YesOrNo.NO).equals(YesOrNo.YES);
    }
}
