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
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.PreSubmitCallbackDispatcher;

@ExtendWith(MockitoExtension.class)
class PreSubmitCallbackControllerTest {

    @Mock private PreSubmitCallbackDispatcher<AsylumCase> callbackDispatcher;
    @Mock private PreSubmitCallbackResponse<CaseData> callbackResponse;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;

    private PreSubmitCallbackController preSubmitCallbackController;

    @BeforeEach
    public void setUp() {
        preSubmitCallbackController =
            new PreSubmitCallbackController(callbackDispatcher);
    }

    @Test
    void should_dispatch_about_to_start_callback_then_dispatch_then_return_response() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        doReturn(callbackResponse)
            .when(callbackDispatcher)
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);
        ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> actualResponse =
            preSubmitCallbackController.ccdAboutToStart(callback);
        assertNotNull(actualResponse);
        verify(callbackDispatcher, times(1)).handle(
            PreSubmitCallbackStage.ABOUT_TO_START,
            callback
        );
    }

    @Test
    void should_not_allow_null_constructor_arguments() {
        assertThatThrownBy(() -> new PreSubmitCallbackController(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackDispatcher must not be null");
    }
}
