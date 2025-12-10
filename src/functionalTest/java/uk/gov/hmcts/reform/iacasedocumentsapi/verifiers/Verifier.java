package uk.gov.hmcts.reform.iacasedocumentsapi.verifiers;

import java.util.Map;

public interface Verifier {

    void verify(
        long testCaseId,
        boolean isAsylumCase,
        Map<String, Object> scenario,
        Map<String, Object> expectedResponse,
        Map<String, Object> actualResponse
    );
}
