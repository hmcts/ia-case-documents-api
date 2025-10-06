package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.postsubmit;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Optional;
import java.util.function.BiPredicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Message;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PostSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.ErrorHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class PostSubmitNotificationHandlerTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    NotificationGenerator notificationGenerator;
    @Mock
    BiPredicate<PostSubmitCallbackStage, Callback<AsylumCase>> canHandle;
    @Mock
    ErrorHandler<AsylumCase> errorHandler;

    private PostSubmitCallbackStage callbackStage = PostSubmitCallbackStage.CCD_SUBMITTED;
    private PostSubmitNotificationHandler notificationHandler;
    private Message expectedMessage = new Message("success", "success");

    @BeforeEach
    void setUp() {
        notificationHandler = new PostSubmitNotificationHandler(canHandle, Collections.singletonList(notificationGenerator));
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
    }

    @Test
    void should_generate_notification_when_event_can_be_handled() {
        when(callback.getEvent()).thenReturn(Event.EDIT_DOCUMENTS);
        when(canHandle.test(callbackStage, callback)).thenReturn(true);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(notificationGenerator.getSuccessMessage()).thenReturn(expectedMessage);
        PostSubmitCallbackResponse response = notificationHandler.handle(callbackStage, callback);

        assertEquals("success", response.getConfirmationHeader().get());
        assertEquals(asylumCase.toString(), response.getConfirmationBody().get());
        verify(notificationGenerator).generate(callback);
    }

    @Test
    void should_return_default_confirmation_when_no_custom_message_is_given() {
        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
        when(canHandle.test(callbackStage, callback)).thenReturn(true);
        when(notificationGenerator.getSuccessMessage()).thenReturn(new Message());
        PostSubmitCallbackResponse response = notificationHandler.handle(callbackStage, callback);

        assertEquals("success", response.getConfirmationHeader().get());
        assertEquals("success", response.getConfirmationBody().get());
        assertEquals(Optional.ofNullable("success"), response.getConfirmationHeader());
        assertEquals(Optional.ofNullable("success"), response.getConfirmationBody());
        verify(notificationGenerator).generate(callback);
    }

    @Test
    void should_not_generate_notification_when_cannot_handle_event() {
        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
        when(canHandle.test(callbackStage, callback)).thenReturn(false);

        assertThatThrownBy(() -> notificationHandler.handle(callbackStage, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");

        verifyNoInteractions(notificationGenerator);
    }

    @Test
    void should_return_false_when_cannot_handle_event() {
        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
        when(canHandle.test(callbackStage, callback)).thenReturn(false);

        assertEquals(false, notificationHandler.canHandle(callbackStage, callback));
    }

    @Test
    void should_return_false_when_skip_event() {
        when(callback.getEvent()).thenReturn(Event.NOC_REQUEST_BAIL);
        assertEquals(false, notificationHandler.canHandle(callbackStage, callback));
    }

    @Test
    void should_throw_exception_when_callback_stage_is_null() {
        assertThatThrownBy(() -> notificationHandler.canHandle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");
    }

    @Test
    void should_throw_exception_when_callback_is_null() {
        assertThatThrownBy(() -> notificationHandler.canHandle(callbackStage, null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    void should_catch_exception_and_invoke_error_handler() {
        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
        when(canHandle.test(callbackStage, callback)).thenReturn(true);
        String message = "exception happened";
        Throwable exception = new RuntimeException(message);
        doThrow(exception).when(notificationGenerator).generate(callback);
        notificationHandler =
            new PostSubmitNotificationHandler(canHandle, Collections.singletonList(notificationGenerator), errorHandler);

        notificationHandler.handle(callbackStage, callback);

        verify(errorHandler).accept(callback, exception);
    }

    @Test
    void should_re_throw_exception_from_generator() {
        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
        when(canHandle.test(callbackStage, callback)).thenReturn(true);
        String message = "exception happened";
        doThrow(new RuntimeException(message)).when(notificationGenerator).generate(callback);
        notificationHandler = new PostSubmitNotificationHandler(canHandle, Collections.singletonList(notificationGenerator));

        assertThatThrownBy(() -> notificationHandler.handle(callbackStage, callback))
            .isExactlyInstanceOf(RuntimeException.class)
            .hasMessage(message);
    }
}
