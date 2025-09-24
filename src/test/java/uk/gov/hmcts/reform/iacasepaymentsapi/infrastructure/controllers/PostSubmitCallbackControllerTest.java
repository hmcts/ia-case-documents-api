package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers;

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
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PostSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.PostSubmitCallbackDispatcher;

@ExtendWith(MockitoExtension.class)
class PostSubmitCallbackControllerTest {

    @Mock
    private PostSubmitCallbackDispatcher<AsylumCase> callbackDispatcher;
    @Mock
    private PostSubmitCallbackResponse callbackResponse;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    private PostSubmitCallbackStage callbackStage = PostSubmitCallbackStage.CCD_SUBMITTED;

    private PostSubmitCallbackController postSubmitCallbackController;

    @BeforeEach
    public void setUp() {
        postSubmitCallbackController =
            new PostSubmitCallbackController(
                callbackDispatcher
            );
    }

    @Test
    void should_dispatch_callback_then_return_response() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);

        doReturn(callbackResponse)
            .when(callbackDispatcher)
            .handle(callbackStage, callback);

        ResponseEntity<PostSubmitCallbackResponse> actualResponse =
            postSubmitCallbackController.ccdSubmitted(callback);

        assertNotNull(actualResponse);

        verify(callbackDispatcher, times(1)).handle(callbackStage, callback);
    }

    @Test
    void should_not_allow_null_constructor_arguments() {

        assertThatThrownBy(() -> new PostSubmitCallbackController(null))
            .hasMessage("callbackDispatcher must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
