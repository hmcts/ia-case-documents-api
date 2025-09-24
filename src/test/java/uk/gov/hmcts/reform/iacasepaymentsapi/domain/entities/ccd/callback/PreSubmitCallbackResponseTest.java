package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseData;

@ExtendWith(MockitoExtension.class)
class PreSubmitCallbackResponseTest {

    @Mock private CaseData caseData;

    private PreSubmitCallbackResponse<CaseData> preSubmitCallbackResponse;

    @BeforeEach
    public void setUp() {

        preSubmitCallbackResponse = new PreSubmitCallbackResponse<>(caseData);
    }

    @Test
    void should_hold_onto_values() {

        assertEquals(caseData, preSubmitCallbackResponse.getData());
    }

    @Test
    void should_store_distinct_errors() {

        List<String> someErrors = Arrays.asList("error3", "error4");
        List<String> someMoreErrors = Arrays.asList("error4", "error1");

        assertTrue(preSubmitCallbackResponse.getErrors().isEmpty());

        preSubmitCallbackResponse.addErrors(someErrors);
        preSubmitCallbackResponse.addErrors(someMoreErrors);

        String[] storedErrors =
            preSubmitCallbackResponse
                .getErrors()
                .toArray(new String[0]);

        assertEquals(3, storedErrors.length);
        assertEquals("error3", storedErrors[0]);
        assertEquals("error4", storedErrors[1]);
        assertEquals("error1", storedErrors[2]);
    }
}
