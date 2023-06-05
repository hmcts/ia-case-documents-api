package uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
public class AsylumCaseUtilsTest {

    @Mock
    private AsylumCase asylumCase;

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
    void should_return_correct_value_for_isAdmin(YesOrNo yesOrNo) {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(yesOrNo));

        if (yesOrNo.equals(YES)) {
            assertTrue(AsylumCaseUtils.isInternalCase(asylumCase));
        } else {
            assertFalse(AsylumCaseUtils.isInternalCase(asylumCase));
        }

    }
}

