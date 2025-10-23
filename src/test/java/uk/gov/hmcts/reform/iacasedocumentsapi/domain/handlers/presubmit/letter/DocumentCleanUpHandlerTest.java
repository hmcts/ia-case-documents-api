package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_NOTIFICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority.LATEST;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class DocumentCleanUpHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private DocumentCleanUpHandler documentCleanUpHandler;

    @BeforeEach
    void setUp() {
        documentCleanUpHandler = new DocumentCleanUpHandler();
    }

    @Test
    void should_have_latest_dispatch_priority() {
        assertEquals(LATEST, documentCleanUpHandler.getDispatchPriority());
    }

    @Test
    void should_handle_all_callback_stages_and_events() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
            for (Event event : Event.values()) {
                when(callback.getEvent()).thenReturn(event);

                boolean canHandle = documentCleanUpHandler.canHandle(callbackStage, callback);

                assertTrue(canHandle);
            }
        }
    }

    @Test
    void should_clean_up_letter_notification_documents() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            documentCleanUpHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).clear(LETTER_NOTIFICATION_DOCUMENTS);
    }

    @Test
    void should_clean_up_documents_for_any_event() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            documentCleanUpHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).clear(LETTER_NOTIFICATION_DOCUMENTS);
    }

    @Test
    void should_clean_up_documents_for_any_stage() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            documentCleanUpHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).clear(LETTER_NOTIFICATION_DOCUMENTS);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        // Since canHandle always returns true, we need to simulate a scenario where it would return false
        // by using a mock that overrides the behavior
        DocumentCleanUpHandler mockHandler = new DocumentCleanUpHandler() {
            @Override
            public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
                return false;
            }
        };

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> mockHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_callback_stage() {
        assertThatThrownBy(() -> documentCleanUpHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_allow_null_callback() {
        assertThatThrownBy(() -> documentCleanUpHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_allow_null_callback_stage_in_handle() {
        assertThatThrownBy(() -> documentCleanUpHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_allow_null_callback_in_handle() {
        assertThatThrownBy(() -> documentCleanUpHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_clean_up_documents_multiple_times_idempotently() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        // Call handle multiple times
        PreSubmitCallbackResponse<AsylumCase> callbackResponse1 =
            documentCleanUpHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse2 =
            documentCleanUpHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse1);
        assertNotNull(callbackResponse2);
        assertEquals(asylumCase, callbackResponse1.getData());
        assertEquals(asylumCase, callbackResponse2.getData());

        // Verify clear was called twice
        verify(asylumCase, times(2)).clear(LETTER_NOTIFICATION_DOCUMENTS);
    }

    @Test
    void should_return_same_asylum_case_instance() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.END_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            documentCleanUpHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        assertTrue(callbackResponse.getData() == asylumCase); // Reference equality check
    }
}