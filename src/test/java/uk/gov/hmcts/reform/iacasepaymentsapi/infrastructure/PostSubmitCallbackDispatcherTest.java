package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PostSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.CcdEventAuthorizor;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class PostSubmitCallbackDispatcherTest {

    @Mock
    private CcdEventAuthorizor ccdEventAuthorizor;
    @Mock
    private PostSubmitCallbackHandler<CaseData> handler1;
    @Mock
    private PostSubmitCallbackHandler<CaseData> handler2;
    @Mock
    private PostSubmitCallbackHandler<CaseData> handler3;
    @Mock
    private Callback<CaseData> callback;
    @Mock
    private CaseDetails<CaseData> caseDetails;
    @Mock
    private CaseData caseData;


    private String header1 = "Some header 1";
    private String body1 = "Some body 1";

    private String header2 = "Some header 2";
    private String body2 = "Some body 2";

    private String header3 = "Some header 3";
    private String body3 = "Some body 3";


    @Mock
    private PostSubmitCallbackResponse response1;
    @Mock
    private PostSubmitCallbackResponse response2;
    @Mock
    private PostSubmitCallbackResponse response3;

    private PostSubmitCallbackDispatcher<CaseData> postSubmitCallbackDispatcher;

    @BeforeEach
    public void setUp() {
        postSubmitCallbackDispatcher = new PostSubmitCallbackDispatcher<>(
            ccdEventAuthorizor,
            Arrays.asList(
                handler1,
                handler2,
                handler3
            )
        );
    }

    @Test
    void should_only_dispatch_callback_to_handlers_that_can_handle_it() {

        for (PostSubmitCallbackStage callbackStage : PostSubmitCallbackStage.values()) {

            when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(caseData);

            when(response1.getConfirmationHeader()).thenReturn(Optional.of(header1));
            when(response1.getConfirmationBody()).thenReturn(Optional.of(body1));

            when(response2.getConfirmationHeader()).thenReturn(Optional.of(header2));
            when(response2.getConfirmationBody()).thenReturn(Optional.of(body2));

            when(response3.getConfirmationHeader()).thenReturn(Optional.of(header3));
            when(response3.getConfirmationBody()).thenReturn(Optional.of(body3));

            when(handler1.canHandle(eq(callbackStage), any(Callback.class))).thenReturn(false);
            when(handler1.handle(eq(callbackStage), any(Callback.class))).thenReturn(response1);

            when(handler2.canHandle(eq(callbackStage), any(Callback.class))).thenReturn(false);
            when(handler2.handle(eq(callbackStage), any(Callback.class))).thenReturn(response2);

            when(handler3.canHandle(eq(callbackStage), any(Callback.class))).thenReturn(true);
            when(handler3.handle(eq(callbackStage), any(Callback.class))).thenReturn(response3);

            PostSubmitCallbackResponse callbackResponse =
                postSubmitCallbackDispatcher.handle(callbackStage, callback);

            assertNotNull(callbackResponse);
            assertEquals(Optional.of(body3), callbackResponse.getConfirmationBody());

            verify(ccdEventAuthorizor, times(1)).throwIfNotAuthorized(Event.SUBMIT_APPEAL);

            verify(handler1, times(1)).canHandle(eq(callbackStage), any(Callback.class));
            verify(handler1, times(0)).handle(eq(callbackStage), any(Callback.class));

            verify(handler2, times(1)).canHandle(eq(callbackStage), any(Callback.class));
            verify(handler2, times(0)).handle(eq(callbackStage), any(Callback.class));

            verify(handler3, times(1)).canHandle(eq(callbackStage), any(Callback.class));
            verify(handler3, times(1)).handle(eq(callbackStage), any(Callback.class));

            reset(ccdEventAuthorizor, handler1, handler2, handler3);
        }
    }

    @Test
    void should_not_dispatch_to_handlers_if_user_not_authorized_for_event() {

        for (PostSubmitCallbackStage callbackStage : PostSubmitCallbackStage.values()) {

            when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

            doThrow(AccessDeniedException.class)
                .when(ccdEventAuthorizor)
                .throwIfNotAuthorized(Event.SUBMIT_APPEAL);

            assertThatThrownBy(() -> postSubmitCallbackDispatcher.handle(callbackStage, callback))
                .isExactlyInstanceOf(AccessDeniedException.class);

            verify(ccdEventAuthorizor, times(1)).throwIfNotAuthorized(Event.SUBMIT_APPEAL);

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
    void should_not_error_if_no_handlers_are_provided() {

        PostSubmitCallbackDispatcher<CaseData> callbackDispatcher =
            new PostSubmitCallbackDispatcher<>(ccdEventAuthorizor, Collections.emptyList());

        for (PostSubmitCallbackStage callbackStage : PostSubmitCallbackStage.values()) {

            try {

                when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(caseDetails.getCaseData()).thenReturn(caseData);

                PostSubmitCallbackResponse callbackResponse =
                    callbackDispatcher
                        .handle(callbackStage, callback);

                assertNotNull(callbackResponse);

                verify(ccdEventAuthorizor, times(1)).throwIfNotAuthorized(Event.SUBMIT_APPEAL);

                reset(ccdEventAuthorizor);

            } catch (Exception e) {
                fail("Should not have thrown any exception");
            }
        }
    }

    @Test
    void should_not_allow_null_ccd_event_authorizor() {
        List<PostSubmitCallbackHandler<CaseData>> postSubmitCallbackHandlers = Collections.emptyList();
        assertThatThrownBy(() -> new PostSubmitCallbackDispatcher<>(null, postSubmitCallbackHandlers))
            .hasMessage("ccdEventAuthorizor must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_allow_null_handlers() {

        assertThatThrownBy(() -> new PostSubmitCallbackDispatcher<>(ccdEventAuthorizor, null))
            .hasMessage("callbackHandlers must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> postSubmitCallbackDispatcher.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> postSubmitCallbackDispatcher.handle(PostSubmitCallbackStage.CCD_SUBMITTED, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
