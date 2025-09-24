package uk.gov.hmcts.reform.iacasepaymentsapi.verifiers;

import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasepaymentsapi.util.MapFieldAssertor;
import uk.gov.hmcts.reform.iacasepaymentsapi.util.MapValueExtractor;

@Component
@SuppressWarnings("unchecked")
public class CaseDataVerifier implements Verifier {

    public void verify(
        long testCaseId,
        Map<String, Object> scenario,
        Map<String, Object> expectedResponse,
        Map<String, Object> actualResponse
    ) {
        String description = MapValueExtractor.extract(scenario, "description");

        MapFieldAssertor.assertFields(expectedResponse, actualResponse, (description + ": "));
    }
}
