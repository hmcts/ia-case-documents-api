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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PreSubmitCallbackDispatcher;

@ExtendWith({MockitoExtension.class})
public class BailPreSubmitCallbackControllerTest {

    @Mock private PreSubmitCallbackDispatcher<BailCase> callbackDispatcher;
    @Mock private PreSubmitCallbackResponse<BailCase> callbackResponse;
    @Mock private Callback<BailCase> callback;
    @Mock private CaseDetails<BailCase> caseDetails;

    private BailPreSubmitCallbackController bailPreSubmitCallbackController;

    @BeforeEach
    public void setUp() {
        bailPreSubmitCallbackController = new BailPreSubmitCallbackController(callbackDispatcher);
    }

    @Test
    public void should_not_allow_null_args_in_constructor() {
        assertThatThrownBy(() -> new BailPreSubmitCallbackController(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackDispatcher must not be null");
    }

    @Test
    public void should_dispatch_about_to_start_callback_and_return_response() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        doReturn(callbackResponse)
            .when(callbackDispatcher)
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        ResponseEntity<PreSubmitCallbackResponse<BailCase>> actualResponse = bailPreSubmitCallbackController.ccdAboutToStart(callback);

        assertNotNull(actualResponse);
        verify(callbackDispatcher, times(1)).handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);
    }

    @Test
    public void should_dispatch_about_to_submit_callback_and_return_response() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        doReturn(callbackResponse)
            .when(callbackDispatcher)
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        ResponseEntity<PreSubmitCallbackResponse<BailCase>> actualResponse = bailPreSubmitCallbackController.ccdAboutToSubmit(callback);

        assertNotNull(actualResponse);
        verify(callbackDispatcher, times(1)).handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
    }


}
