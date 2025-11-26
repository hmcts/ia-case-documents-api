package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.BailCaseUtils;

@MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
public class BailCaseUtilsTest {

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    void should_return_correct_value_for_ima_enable(YesOrNo fieldValue) {
        // given
        BailCase bailCase = new BailCase();
        bailCase.write(BailCaseFieldDefinition.IS_IMA_ENABLED, fieldValue);

        if (fieldValue.equals(YesOrNo.YES)) {
            assertTrue(BailCaseUtils.isImaEnabled(bailCase));
        } else {
            assertFalse(BailCaseUtils.isImaEnabled(bailCase));
        }
    }
}
