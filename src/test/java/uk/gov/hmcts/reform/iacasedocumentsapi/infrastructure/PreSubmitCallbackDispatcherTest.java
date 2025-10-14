package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.CcdEventAuthorizor;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class PreSubmitCallbackDispatcherTest {

    @Mock private CcdEventAuthorizor ccdEventAuthorizor;
    @Mock private PreSubmitCallbackHandler<CaseData> handler1;
    @Mock private PreSubmitCallbackHandler<CaseData> handler2;
    @Mock private PreSubmitCallbackHandler<CaseData> handler3;
    @Mock private Callback<CaseData> callback;
    @Mock private CaseDetails<CaseData> caseDetails;
    @Mock private CaseData caseData;
    @Mock private CaseData caseDataMutation1;
    @Mock private CaseData caseDataMutation2;
    @Mock private CaseData caseDataMutation3;
    @Mock private PreSubmitCallbackResponse<CaseData> response1;
    @Mock private PreSubmitCallbackResponse<CaseData> response2;
    @Mock private PreSubmitCallbackResponse<CaseData> response3;

    private PreSubmitCallbackDispatcher<CaseData> preSubmitCallbackDispatcher;

    @BeforeEach
    public void setUp() {

        preSubmitCallbackDispatcher = new PreSubmitCallbackDispatcher<>(
            ccdEventAuthorizor,
            Arrays.asList(handler1,
                          handler2,
                          handler3)
        );
    }

    @Test
    void should_dispatch_callback_to_handlers_according_to_priority_collecting_errors() {

        Set<String> expectedErrors =
            ImmutableSet.of("error1", "error2", "error3", "error4");

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
            when(caseDetails.getCaseData()).thenReturn(caseData);

            when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(caseData);

            when(response1.getData()).thenReturn(caseDataMutation1);
            when(response1.getErrors()).thenReturn(ImmutableSet.of("error1"));

            when(response2.getData()).thenReturn(caseDataMutation2);
            when(response2.getErrors()).thenReturn(ImmutableSet.of("error2", "error3"));

            when(response3.getData()).thenReturn(caseDataMutation3);
            when(response3.getErrors()).thenReturn(ImmutableSet.of("error4"));

            when(handler1.getDispatchPriority()).thenReturn(DispatchPriority.EARLY);
            when(handler1.canHandle(callbackStage, callback)).thenReturn(true);
            when(handler1.handle(callbackStage, callback)).thenReturn(response1);

            when(handler2.getDispatchPriority()).thenReturn(DispatchPriority.LATE);
            when(handler2.canHandle(callbackStage, callback)).thenReturn(true);
            when(handler2.handle(callbackStage, callback)).thenReturn(response2);

            when(handler3.getDispatchPriority()).thenReturn(DispatchPriority.EARLY);
            when(handler3.canHandle(callbackStage, callback)).thenReturn(true);
            when(handler3.handle(callbackStage, callback)).thenReturn(response3);

            PreSubmitCallbackResponse<CaseData> callbackResponse =
                preSubmitCallbackDispatcher.handle(callbackStage, callback);

            Assertions.assertNotNull(callbackResponse);
            Assertions.assertEquals(caseData, callbackResponse.getData());
            Assertions.assertEquals(callbackResponse.getErrors(), expectedErrors);

            verify(ccdEventAuthorizor, times(1)).throwIfNotAuthorized(Event.PAYMENT_APPEAL);

            InOrder inOrder = inOrder(handler1, handler2, handler3);

            inOrder.verify(handler1, times(1)).canHandle(callbackStage, callback);
            inOrder.verify(handler1, times(1)).handle(callbackStage, callback);

            inOrder.verify(handler3, times(1)).canHandle(callbackStage, callback);
            inOrder.verify(handler3, times(1)).handle(callbackStage, callback);

            inOrder.verify(handler2, times(1)).canHandle(callbackStage, callback);
            inOrder.verify(handler2, times(1)).handle(callbackStage, callback);

            reset(ccdEventAuthorizor, handler1, handler2, handler3);
        }
    }

    @Test
    void should_only_dispatch_callback_to_handlers_that_can_handle_it() {

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
            when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(caseData);

            when(response1.getData()).thenReturn(caseData);
            when(response1.getErrors()).thenReturn(Collections.emptySet());

            when(response2.getData()).thenReturn(caseData);
            when(response2.getErrors()).thenReturn(Collections.emptySet());

            when(response3.getData()).thenReturn(caseData);
            when(response3.getErrors()).thenReturn(Collections.emptySet());

            when(handler1.getDispatchPriority()).thenReturn(DispatchPriority.EARLY);
            when(handler1.canHandle(callbackStage, callback)).thenReturn(false);
            when(handler1.handle(callbackStage, callback)).thenReturn(response1);

            when(handler2.getDispatchPriority()).thenReturn(DispatchPriority.LATE);
            when(handler2.canHandle(callbackStage, callback)).thenReturn(false);
            when(handler2.handle(callbackStage, callback)).thenReturn(response2);

            when(handler3.getDispatchPriority()).thenReturn(DispatchPriority.EARLY);
            when(handler3.canHandle(callbackStage, callback)).thenReturn(true);
            when(handler3.handle(callbackStage, callback)).thenReturn(response3);

            PreSubmitCallbackResponse<CaseData> callbackResponse =
                preSubmitCallbackDispatcher.handle(callbackStage, callback);

            Assertions.assertNotNull(callbackResponse);
            Assertions.assertEquals(caseData, callbackResponse.getData());
            Assertions.assertEquals(callbackResponse.getErrors(), Set.of());

            verify(ccdEventAuthorizor, times(1)).throwIfNotAuthorized(Event.PAYMENT_APPEAL);

            verify(handler1, times(1)).canHandle(callbackStage, callback);
            verify(handler1, times(0)).handle(callbackStage, callback);

            verify(handler2, times(1)).canHandle(callbackStage, callback);
            verify(handler2, times(0)).handle(callbackStage, callback);

            verify(handler3, times(1)).canHandle(callbackStage, callback);
            verify(handler3, times(1)).handle(callbackStage, callback);

            reset(ccdEventAuthorizor, handler1, handler2, handler3);
        }
    }

    @Test
    public void should_not_dispatch_to_handlers_if_user_not_authorized_for_event() {

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

            when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);

            doThrow(AccessDeniedException.class)
                .when(ccdEventAuthorizor)
                .throwIfNotAuthorized(Event.PAYMENT_APPEAL);

            assertThatThrownBy(() -> preSubmitCallbackDispatcher.handle(callbackStage, callback))
                .isExactlyInstanceOf(AccessDeniedException.class);

            verify(ccdEventAuthorizor, times(1)).throwIfNotAuthorized(Event.PAYMENT_APPEAL);

            verify(handler1, never()).canHandle(any(), any());
            verify(handler1, never()).handle(any(), any());
            verify(handler2, never()).canHandle(any(), any());
            verify(handler2, never()).handle(any(), any());
            verify(handler3, never()).canHandle(any(), any());
            verify(handler3, never()).handle(any(), any());

            reset(ccdEventAuthorizor, handler1, handler2, handler3);
        }
    }


    @Test
    void should_not_error_if_no_handler_is_provided() {

        PreSubmitCallbackDispatcher<CaseData> preSubmitCallbackDispatcher =
            new PreSubmitCallbackDispatcher<>(ccdEventAuthorizor, Collections.emptyList());

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

            try {
                when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(caseDetails.getCaseData()).thenReturn(caseData);

                PreSubmitCallbackResponse<CaseData> callbackResponse =
                    preSubmitCallbackDispatcher.handle(callbackStage, callback);

                Assertions.assertNotNull(callbackResponse);
                Assertions.assertEquals(caseData, callbackResponse.getData());
                Assertions.assertEquals(callbackResponse.getErrors(), Set.of());

                verify(ccdEventAuthorizor, times(1)).throwIfNotAuthorized(Event.PAYMENT_APPEAL);

                reset(ccdEventAuthorizor);
            } catch (Exception e) {
                fail("Should not have thrown any exception");
            }
        }
    }

    @Test
    void should_not_allow_null_handlers() {

        assertThatThrownBy(() -> new PreSubmitCallbackDispatcher<>(ccdEventAuthorizor, null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackHandlers must not be null");
    }

    @Test
    void should_not_allow_null_ccd_event_authorizor() {

        assertThatThrownBy(() -> new PreSubmitCallbackDispatcher<>(null, Collections.emptyList()))
            .hasMessage("ccdEventAuthorizor must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_allow_null_values() {

        assertThatThrownBy(() -> preSubmitCallbackDispatcher.handle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");

        assertThatThrownBy(() -> preSubmitCallbackDispatcher.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

}
