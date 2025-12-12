package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitPaymentsCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.AppealSubmissionCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.HearingNoticeCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.payments.FeeLookupHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.CcdEventAuthorizor;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AsylumPreSubmitCallbackDispatcherTest {

    @Mock private CcdEventAuthorizor ccdEventAuthorizor;
    @Mock private PreSubmitCallbackHandler<AsylumCase> handler1;
    @Mock private PreSubmitCallbackHandler<AsylumCase> handler2;
    @Mock private PreSubmitCallbackHandler<AsylumCase> handler3;
    @Mock private PreSubmitCallbackHandler<AsylumCase> handler4;
    @Mock private PreSubmitPaymentsCallbackHandler<AsylumCase> handler5;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase caseData;
    @Mock private AsylumCase caseDataMutation1;
    @Mock private AsylumCase caseDataMutation2;
    @Mock private AsylumCase caseDataMutation3;
    @Mock private AsylumCase caseDataMutation4;
    @Mock private AsylumCase caseDataMutation5;
    @Mock private PreSubmitCallbackResponse<AsylumCase> response1;
    @Mock private PreSubmitCallbackResponse<AsylumCase> response2;
    @Mock private PreSubmitCallbackResponse<AsylumCase> response3;
    @Mock private PreSubmitCallbackResponse<AsylumCase> response4;
    @Mock private PreSubmitCallbackResponse<AsylumCase> response5;

    private PreSubmitCallbackDispatcher<AsylumCase> preSubmitCallbackDispatcher;

    @BeforeEach
    void setUp() {
        preSubmitCallbackDispatcher = new AsylumPreSubmitCallbackDispatcher(
            ccdEventAuthorizor,
            Arrays.asList(
                handler1,
                handler2,
                handler3,
                handler4,
                handler5
            )
        );
    }

    @Test
    void should_dispatch_callback_to_handlers_according_to_priority_collecting_any_error_messages() {

        Set<String> expectedErrors =
            ImmutableSet.of("error1", "error2", "error2.3", "error3", "error4", "error5");

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

            when(caseDetails.getCaseData()).thenReturn(caseData);

            when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
            when(callback.getCaseDetails()).thenReturn(caseDetails);

            when(response1.getData()).thenReturn(caseDataMutation1);
            when(response1.getErrors()).thenReturn(ImmutableSet.of("error1"));

            when(response2.getData()).thenReturn(caseDataMutation2);
            when(response2.getErrors()).thenReturn(ImmutableSet.of("error2", "error2.3"));

            when(response3.getData()).thenReturn(caseDataMutation3);
            when(response3.getErrors()).thenReturn(ImmutableSet.of("error3"));

            when(response4.getData()).thenReturn(caseDataMutation4);
            when(response4.getErrors()).thenReturn(ImmutableSet.of("error4"));

            when(response5.getData()).thenReturn(caseDataMutation5);
            when(response5.getErrors()).thenReturn(ImmutableSet.of("error5"));

            when(handler1.getDispatchPriority()).thenReturn(DispatchPriority.EARLY);
            when(handler1.canHandle(eq(callbackStage), any(Callback.class))).thenReturn(true);
            when(handler1.handle(eq(callbackStage), any(Callback.class))).thenReturn(response1);

            when(handler2.getDispatchPriority()).thenReturn(DispatchPriority.LATE);
            when(handler2.canHandle(eq(callbackStage), any(Callback.class))).thenReturn(true);
            when(handler2.handle(eq(callbackStage), any(Callback.class))).thenReturn(response2);

            when(handler3.getDispatchPriority()).thenReturn(DispatchPriority.EARLIEST);
            when(handler3.canHandle(eq(callbackStage), any(Callback.class))).thenReturn(true);
            when(handler3.handle(eq(callbackStage), any(Callback.class))).thenReturn(response3);

            when(handler4.getDispatchPriority()).thenReturn(DispatchPriority.LATEST);
            when(handler4.canHandle(eq(callbackStage), any(Callback.class))).thenReturn(true);
            when(handler4.handle(eq(callbackStage), any(Callback.class))).thenReturn(response4);

            when(handler5.getDispatchPriority()).thenReturn(DispatchPriority.PAYMENTS);
            when(handler5.canHandle(eq(callbackStage), any(Callback.class))).thenReturn(true);
            when(handler5.handle(eq(callbackStage), any(Callback.class))).thenReturn(response5);

            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                preSubmitCallbackDispatcher.handle(callbackStage, callback);

            assertNotNull(callbackResponse);
            assertEquals(caseDataMutation4, callbackResponse.getData());
            assertEquals(expectedErrors, callbackResponse.getErrors());

            verify(ccdEventAuthorizor, times(1)).throwIfNotAuthorized(Event.BUILD_CASE);

            InOrder inOrder = inOrder(handler5, handler3, handler1, handler2, handler4);

            inOrder.verify(handler5, times(1)).canHandle(eq(callbackStage), any(Callback.class));
            inOrder.verify(handler5, times(1)).handle(eq(callbackStage), any(Callback.class));

            inOrder.verify(handler3, times(1)).canHandle(eq(callbackStage), any(Callback.class));
            inOrder.verify(handler3, times(1)).handle(eq(callbackStage), any(Callback.class));

            inOrder.verify(handler1, times(1)).canHandle(eq(callbackStage), any(Callback.class));
            inOrder.verify(handler1, times(1)).handle(eq(callbackStage), any(Callback.class));

            inOrder.verify(handler2, times(1)).canHandle(eq(callbackStage), any(Callback.class));
            inOrder.verify(handler2, times(1)).handle(eq(callbackStage), any(Callback.class));

            inOrder.verify(handler4, times(1)).canHandle(eq(callbackStage), any(Callback.class));
            inOrder.verify(handler4, times(1)).handle(eq(callbackStage), any(Callback.class));

            reset(ccdEventAuthorizor, handler1, handler2, handler3, handler4, handler5);
        }
    }

    @Test
    void should_only_dispatch_callback_to_handlers_that_can_handle_it() {

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

            when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(caseData);

            when(response3.getData()).thenReturn(caseData);
            when(response3.getErrors()).thenReturn(Collections.emptySet());

            when(response4.getData()).thenReturn(caseData);
            when(response4.getErrors()).thenReturn(Collections.emptySet());

            when(response5.getData()).thenReturn(caseData);
            when(response5.getErrors()).thenReturn(Collections.emptySet());

            when(handler1.getDispatchPriority()).thenReturn(DispatchPriority.EARLY);
            when(handler1.canHandle(eq(callbackStage), any(Callback.class))).thenReturn(false);
            when(handler1.handle(eq(callbackStage), any(Callback.class))).thenReturn(response1);

            when(handler2.getDispatchPriority()).thenReturn(DispatchPriority.LATE);
            when(handler2.canHandle(eq(callbackStage), any(Callback.class))).thenReturn(false);
            when(handler2.handle(eq(callbackStage), any(Callback.class))).thenReturn(response2);

            when(handler3.getDispatchPriority()).thenReturn(DispatchPriority.EARLIEST);
            when(handler3.canHandle(eq(callbackStage), any(Callback.class))).thenReturn(true);
            when(handler3.handle(eq(callbackStage), any(Callback.class))).thenReturn(response3);

            when(handler4.getDispatchPriority()).thenReturn(DispatchPriority.LATEST);
            when(handler4.canHandle(eq(callbackStage), any(Callback.class))).thenReturn(true);
            when(handler4.handle(eq(callbackStage), any(Callback.class))).thenReturn(response4);

            when(handler5.getDispatchPriority()).thenReturn(DispatchPriority.PAYMENTS);
            when(handler5.canHandle(eq(callbackStage), any(Callback.class))).thenReturn(true);
            when(handler5.handle(eq(callbackStage), any(Callback.class))).thenReturn(response5);

            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                preSubmitCallbackDispatcher.handle(callbackStage, callback);

            assertNotNull(callbackResponse);
            assertEquals(caseData, callbackResponse.getData());
            assertTrue(callbackResponse.getErrors().isEmpty());

            verify(ccdEventAuthorizor, times(1)).throwIfNotAuthorized(Event.BUILD_CASE);

            verify(handler1, times(1)).canHandle(eq(callbackStage), any(Callback.class));
            verify(handler1, times(0)).handle(eq(callbackStage), any(Callback.class));

            verify(handler2, times(1)).canHandle(eq(callbackStage), any(Callback.class));
            verify(handler2, times(0)).handle(eq(callbackStage), any(Callback.class));

            verify(handler3, times(1)).canHandle(eq(callbackStage), any(Callback.class));
            verify(handler3, times(1)).handle(eq(callbackStage), any(Callback.class));

            verify(handler4, times(1)).canHandle(eq(callbackStage), any(Callback.class));
            verify(handler4, times(1)).handle(eq(callbackStage), any(Callback.class));

            verify(handler5, times(1)).canHandle(eq(callbackStage), any(Callback.class));
            verify(handler5, times(1)).handle(eq(callbackStage), any(Callback.class));

            reset(ccdEventAuthorizor, handler1, handler2, handler3, handler4, handler5);
        }
    }

    @Test
    void should_not_dispatch_to_handlers_if_user_not_authorized_for_event() {

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

            when(callback.getEvent()).thenReturn(Event.BUILD_CASE);

            doThrow(AccessDeniedException.class)
                .when(ccdEventAuthorizor)
                .throwIfNotAuthorized(Event.BUILD_CASE);

            assertThatThrownBy(() -> preSubmitCallbackDispatcher.handle(callbackStage, callback))
                .isExactlyInstanceOf(AccessDeniedException.class);

            verify(ccdEventAuthorizor, times(1)).throwIfNotAuthorized(Event.BUILD_CASE);

            verify(handler1, never()).canHandle(any(), any());
            verify(handler1, never()).handle(any(), any());
            verify(handler2, never()).canHandle(any(), any());
            verify(handler2, never()).handle(any(), any());
            verify(handler3, never()).canHandle(any(), any());
            verify(handler3, never()).handle(any(), any());
            verify(handler4, never()).canHandle(any(), any());
            verify(handler4, never()).handle(any(), any());
            verify(handler5, never()).canHandle(any(), any());
            verify(handler5, never()).handle(any(), any());

            reset(ccdEventAuthorizor, handler1, handler2, handler3, handler4, handler5);
        }
    }

    @Test
    void should_not_error_if_no_handlers_are_provided() {

        AsylumPreSubmitCallbackDispatcher preSubmitCallbackDispatcher =
            new AsylumPreSubmitCallbackDispatcher(ccdEventAuthorizor, Collections.emptyList());

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

            try {

                when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(caseDetails.getCaseData()).thenReturn(caseData);

                PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                    preSubmitCallbackDispatcher
                        .handle(callbackStage, callback);

                assertNotNull(callbackResponse);
                assertEquals(caseData, callbackResponse.getData());
                assertTrue(callbackResponse.getErrors().isEmpty());

                verify(ccdEventAuthorizor, times(1)).throwIfNotAuthorized(Event.BUILD_CASE);

                reset(ccdEventAuthorizor);

            } catch (Exception e) {
                fail("Should not have thrown any exception");
            }
        }
    }

    @Test
    void should_not_allow_null_ccd_event_authorizor() {

        assertThatThrownBy(() -> new AsylumPreSubmitCallbackDispatcher(null, Collections.emptyList()))
            .hasMessage("ccdEventAuthorizor must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_allow_null_handlers() {

        assertThatThrownBy(() -> new AsylumPreSubmitCallbackDispatcher(ccdEventAuthorizor, null))
            .hasMessage("callbackHandlers must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> preSubmitCallbackDispatcher.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> preSubmitCallbackDispatcher.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_sort_handlers_by_name() {
        PreSubmitCallbackHandler<AsylumCase> h1 = new AppealSubmissionCreator(
            mock(DocumentCreator.class), mock(DocumentCreator.class), mock(DocumentCreator.class), mock(DocumentHandler.class)
        );


        PreSubmitCallbackHandler<AsylumCase> h3 = new HearingNoticeCreator(
                mock(DocumentCreator.class), mock(DocumentCreator.class), mock(DocumentCreator.class), mock(DocumentHandler.class),
                mock(FeatureToggler.class), mock(DocumentReceiver.class),
                mock(DocumentsAppender.class), mock(Appender.class)
        );

        PreSubmitCallbackDispatcher<AsylumCase> dispatcher = new AsylumPreSubmitCallbackDispatcher(
            ccdEventAuthorizor,
            Arrays.asList(
                h3,
                h1
            )
        );

        List<PreSubmitCallbackHandler<AsylumCase>> sortedDispatcher =
            (List<PreSubmitCallbackHandler<AsylumCase>>) ReflectionTestUtils.getField(dispatcher, "sortedCallbackHandlers");

        assertEquals(2, sortedDispatcher.size());
        assertEquals(h1, sortedDispatcher.get(0));
        assertEquals(h3, sortedDispatcher.get(1));
    }

    @Test
    void payments_handler_should_have_payments_priority() {
        PreSubmitCallbackHandler<AsylumCase> paymentsHandler = new FeeLookupHandler(mock(FeeService.class));
        assertEquals(DispatchPriority.PAYMENTS, paymentsHandler.getDispatchPriority());
    }
}
