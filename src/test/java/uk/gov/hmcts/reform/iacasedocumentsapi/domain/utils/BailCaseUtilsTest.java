package uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class BailCaseUtilsTest {
    @Mock
    private BailCase bailCase;

    @Test
    void isAdmin_should_return_true() {
        when(bailCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(BailCaseUtils.isInternalCase(bailCase));
    }

    @Test
    void isAdmin_should_return_false() {
        when(bailCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(NO));
        assertFalse(BailCaseUtils.isInternalCase(bailCase));
    }


    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    void should_return_correct_value_for_ima_enable(YesOrNo fieldValue) {
        // given
        BailCase bailCase = new BailCase();
        bailCase.write(BailCaseFieldDefinition.IS_IMA_ENABLED, fieldValue);

        if (fieldValue.equals(YesOrNo.YES)) {
            Assert.assertTrue(BailCaseUtils.isImaEnabled(bailCase));
        } else {
            Assert.assertFalse(BailCaseUtils.isImaEnabled(bailCase));
        }
    }
}
