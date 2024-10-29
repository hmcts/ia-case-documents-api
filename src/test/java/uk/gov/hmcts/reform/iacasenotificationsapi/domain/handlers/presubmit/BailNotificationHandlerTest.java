package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.function.BiPredicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.ErrorHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.BailNotificationGenerator;


@ExtendWith(MockitoExtension.class)
public class BailNotificationHandlerTest {

    @Mock
    Callback<BailCase> callback;
    @Mock
    CaseDetails<BailCase> caseDetails;
    @Mock
    BailCase bailCase;
    @Mock
    BailNotificationGenerator bailNotificationGenerator;
    @Mock
    BiPredicate<PreSubmitCallbackStage, Callback<BailCase>> canHandle;
    @Mock
    ErrorHandler<BailCase> errorHandler;

    private final PreSubmitCallbackStage callbackStage = PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
    private BailNotificationHandler bailNotificationHandler;

    @BeforeEach
    public void setup() {
        bailNotificationHandler = new BailNotificationHandler(canHandle, Collections.singletonList(bailNotificationGenerator));
    }

    @Test
    public void should_generate_notification_when_event_can_be_handled() {
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(canHandle.test(callbackStage, callback)).thenReturn(true);
        PreSubmitCallbackResponse<BailCase> response = bailNotificationHandler.handle(callbackStage, callback);

        assertEquals(bailCase, response.getData());
        verify(bailNotificationGenerator).generate(callback);
    }

    @Test
    public void should_not_generate_notification_when_cannot_handle_event() {
        when(canHandle.test(callbackStage, callback)).thenReturn(false);

        assertThatThrownBy(() -> bailNotificationHandler.handle(callbackStage, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");

        verifyNoInteractions(bailNotificationGenerator);
    }

    @Test
    public void should_return_false_when_cannot_handle_event() {
        when(canHandle.test(callbackStage, callback)).thenReturn(false);

        assertFalse(bailNotificationHandler.canHandle(callbackStage, callback));
    }

    @Test
    public void should_throw_exception_when_callback_stage_is_null() {
        assertThatThrownBy(() -> bailNotificationHandler.canHandle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");
    }

    @Test
    public void should_throw_exception_when_callback_is_null() {
        assertThatThrownBy(() -> bailNotificationHandler.canHandle(callbackStage, null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    public void should_catch_exception_and_invoke_error_handler() {
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(canHandle.test(callbackStage, callback)).thenReturn(true);
        String message = "exception happened";
        Throwable exception = new RuntimeException(message);
        doThrow(exception).when(bailNotificationGenerator).generate(callback);
        bailNotificationHandler =
            new BailNotificationHandler(canHandle, Collections.singletonList(bailNotificationGenerator), errorHandler);

        bailNotificationHandler.handle(callbackStage, callback);

        verify(errorHandler).accept(callback, exception);
    }

    @Test
    public void should_re_throw_exception_from_generator() {

        when(canHandle.test(callbackStage, callback)).thenReturn(true);
        String message = "exception happened";
        doThrow(new RuntimeException(message)).when(bailNotificationGenerator).generate(callback);
        bailNotificationHandler = new BailNotificationHandler(canHandle, Collections.singletonList(bailNotificationGenerator));

        assertThatThrownBy(() -> bailNotificationHandler.handle(callbackStage, callback))
            .isExactlyInstanceOf(RuntimeException.class)
            .hasMessage(message);
    }
}
