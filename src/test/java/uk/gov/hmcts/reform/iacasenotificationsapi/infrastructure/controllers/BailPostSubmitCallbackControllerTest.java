package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.controllers;


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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PostSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PostSubmitCallbackDispatcher;

@ExtendWith(MockitoExtension.class)
class BailPostSubmitCallbackControllerTest {

    @Mock
    private PostSubmitCallbackDispatcher<BailCase> callbackDispatcher;
    @Mock
    private PostSubmitCallbackResponse callbackResponse;
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;

    private PostSubmitCallbackController<BailCase> postSubmitCallbackController;

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
