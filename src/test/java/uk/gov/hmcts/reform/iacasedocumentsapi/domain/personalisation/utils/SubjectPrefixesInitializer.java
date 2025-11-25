package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils;

import org.springframework.test.util.ReflectionTestUtils;

public class SubjectPrefixesInitializer {

    private SubjectPrefixesInitializer() {
        // for checkStyle
    }

    public static void initializePrefixes(Object testClass) {
        ReflectionTestUtils.setField(testClass, "adaPrefix", "Accelerated detained appeal");
        ReflectionTestUtils.setField(testClass, "nonAdaPrefix", "Immigration and Asylum appeal");
    }

    public static void initializePrefixesForInternalAppeal(Object testClass) {
        ReflectionTestUtils.setField(testClass, "adaPrefix", "ADA - SERVE IN PERSON");
        ReflectionTestUtils.setField(testClass, "nonAdaPrefix", "IAFT - SERVE IN PERSON");
    }

    public static void initializePrefixesForInternalAppealByPost(Object testClass) {
        ReflectionTestUtils.setField(testClass, "adaPrefix", "ADA - SERVE BY POST");
        ReflectionTestUtils.setField(testClass, "nonAdaPrefix", "IAFT - SERVE BY POST");
    }
}
