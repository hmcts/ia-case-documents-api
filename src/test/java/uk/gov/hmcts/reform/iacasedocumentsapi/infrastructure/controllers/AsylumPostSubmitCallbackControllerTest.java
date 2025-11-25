package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PostSubmitCallbackDispatcher;

@ExtendWith(MockitoExtension.class)
class AsylumPostSubmitCallbackControllerTest {

    @Mock
    private PostSubmitCallbackDispatcher<AsylumCase> callbackDispatcher;
    @Mock
    private PostSubmitCallbackResponse callbackResponse;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    private PostSubmitCallbackController<AsylumCase> postSubmitCallbackController;

    @BeforeEach
    public void setUp() {
        postSubmitCallbackController =
            new PostSubmitCallbackController<>(
                callbackDispatcher
            );
    }

    @Test
    void should_deserialize_about_to_start_callback_then_dispatch_then_return_response() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);

        doReturn(callbackResponse)
            .when(callbackDispatcher)
            .handle(PostSubmitCallbackStage.CCD_SUBMITTED, callback);

        ResponseEntity<PostSubmitCallbackResponse> actualResponse =
            postSubmitCallbackController.ccdSubmitted(callback);

        assertNotNull(actualResponse);

        verify(callbackDispatcher, times(1)).handle(
            PostSubmitCallbackStage.CCD_SUBMITTED,
            callback
        );
    }

    @Test
    void should_deserialize_about_to_submit_callback_then_dispatch_then_return_response() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);

        doReturn(callbackResponse)
            .when(callbackDispatcher)
            .handle(PostSubmitCallbackStage.CCD_SUBMITTED, callback);

        ResponseEntity<PostSubmitCallbackResponse> actualResponse =
            postSubmitCallbackController.ccdSubmitted(callback);

        assertNotNull(actualResponse);

        verify(callbackDispatcher, times(1)).handle(
            PostSubmitCallbackStage.CCD_SUBMITTED,
            callback
        );
    }

    @Test
    void should_not_allow_null_constructor_arguments() {

        assertThatThrownBy(() -> new PostSubmitCallbackController<>(null))
            .hasMessage("callbackDispatcher must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
