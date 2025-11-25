package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.CommonUtils.convertAsylumCaseFeeValue;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CommonUtilsTest {

    @ParameterizedTest
    @MethodSource("generateCanHandleScenarios")
    void should_correctly_convert_asylum_case_value_to_amount(String asylumCaseValue, String expectedValue) {
        assertEquals(expectedValue, convertAsylumCaseFeeValue(asylumCaseValue));
    }

    private static Stream<Arguments> generateCanHandleScenarios() {
        return Stream.of(
            Arguments.of("1000", "10.00"),
            Arguments.of("", ""),
            Arguments.of("80", "0.80")
        );
    }
}
