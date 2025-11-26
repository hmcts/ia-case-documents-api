package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.NotificationHandler.getEventsToSkip;

import java.util.Collections;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.ErrorHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.NotificationGenerator;


@ExtendWith(MockitoExtension.class)
public class NotificationHandlerTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    NotificationGenerator notificationGenerator;
    @Mock
    BiPredicate<PreSubmitCallbackStage, Callback<AsylumCase>> canHandle;
    @Mock
    ErrorHandler<AsylumCase> errorHandler;

    private PreSubmitCallbackStage callbackStage = PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
    private NotificationHandler notificationHandler;

    @BeforeEach
    public void setup() {

        notificationHandler = new NotificationHandler(canHandle, Collections.singletonList(notificationGenerator));
    }

    @ParameterizedTest
    @MethodSource("includedEvents")
    public void handle_should_generate_notification_when_event_can_be_handled(Event event) {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(canHandle.test(callbackStage, callback)).thenReturn(true);
        PreSubmitCallbackResponse<AsylumCase> response = notificationHandler.handle(callbackStage, callback);

        assertEquals(asylumCase, response.getData());
        verify(notificationGenerator).generate(callback);
    }

    @ParameterizedTest
    @MethodSource("excludedEvents")
    public void handle_should_throw_when_cannot_handle_event(Event event) {
        when(callback.getEvent()).thenReturn(event);

        IllegalStateException response = assertThrows(
            IllegalStateException.class,
            () -> notificationHandler.handle(callbackStage, callback)
        );
        assertEquals("Cannot handle callback", response.getMessage());

        verifyNoInteractions(notificationGenerator);
    }

    @Test
    public void handle_should_throw_when_cannot_handle_function() {
        when(canHandle.test(callbackStage, callback)).thenReturn(false);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        IllegalStateException response = assertThrows(
            IllegalStateException.class,
            () -> notificationHandler.handle(callbackStage, callback)
        );
        assertEquals("Cannot handle callback", response.getMessage());

        verifyNoInteractions(notificationGenerator);
    }

    @ParameterizedTest
    @MethodSource("excludedEvents")
    public void can_handle_should_return_false_when_cannot_handle_event(Event event) {
        when(callback.getEvent()).thenReturn(event);

        assertFalse(notificationHandler.canHandle(callbackStage, callback));
    }

    @ParameterizedTest
    @MethodSource("includedEvents")
    public void can_handle_should_return_true_when_can_handle_event(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(canHandle.test(callbackStage, callback)).thenReturn(false);

        assertFalse(notificationHandler.canHandle(callbackStage, callback));
    }

    @Test
    public void can_handle_should_throw_exception_when_callback_stage_is_null() {
        NullPointerException response = assertThrows(
            NullPointerException.class,
            () -> notificationHandler.canHandle(null, callback)
        );
        assertEquals("callbackStage must not be null", response.getMessage());
    }

    @Test
    public void can_handle_should_throw_exception_when_callback_is_null() {
        NullPointerException response = assertThrows(
            NullPointerException.class,
            () -> notificationHandler.canHandle(callbackStage, null)
        );
        assertEquals("callback must not be null", response.getMessage());
    }

    @Test
    public void handle_should_catch_exception_and_invoke_error_handler() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(canHandle.test(callbackStage, callback)).thenReturn(true);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        String message = "exception happened";
        Throwable exception = new RuntimeException(message);
        doThrow(exception).when(notificationGenerator).generate(callback);
        notificationHandler =
            new NotificationHandler(canHandle, Collections.singletonList(notificationGenerator), errorHandler);

        notificationHandler.handle(callbackStage, callback);

        verify(errorHandler).accept(callback, exception);
    }

    @Test
    public void handle_should_re_throw_exception_from_generator() {
        when(canHandle.test(callbackStage, callback)).thenReturn(true);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        String message = "exception happened";
        doThrow(new RuntimeException(message)).when(notificationGenerator).generate(callback);
        notificationHandler = new NotificationHandler(canHandle, Collections.singletonList(notificationGenerator));

        RuntimeException response = assertThrows(
            RuntimeException.class,
            () -> notificationHandler.handle(callbackStage, callback)
        );
        assertEquals(message, response.getMessage());
    }

    static Stream<Event> includedEvents() {
        return Stream.of(Event.values())
            .filter(event -> !getEventsToSkip().contains(event));
    }

    static Stream<Event> excludedEvents() {
        return getEventsToSkip().stream();
    }
}
