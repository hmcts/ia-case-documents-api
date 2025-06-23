package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AsylumPreSubmitCallbackDispatcher;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AsylumPreSubmitCallbackControllerTest {

    @Mock private AsylumPreSubmitCallbackDispatcher callbackDispatcher;
    @Mock private PreSubmitCallbackResponse<AsylumCase> callbackResponse;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private HttpEntity httpEntity;

    private AsylumPreSubmitCallbackController asylumPreSubmitCallbackController;

    @BeforeEach
    public void setUp() {
        asylumPreSubmitCallbackController =
            new AsylumPreSubmitCallbackController(
                callbackDispatcher
            );
    }

    @Test
    public void should_dispatch_about_to_start_callback_then_return_response() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetails));
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.empty());

        doReturn(callbackResponse)
            .when(callbackDispatcher)
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> actualResponse =
            asylumPreSubmitCallbackController.ccdAboutToStart(callback);

        assertNotNull(actualResponse);

        verify(callbackDispatcher, times(1)).handle(
            PreSubmitCallbackStage.ABOUT_TO_START,
            callback
        );
    }

    @Test
    public void should_dispatch_about_to_start_callback_then_return_response1() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.empty());
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.empty());

        doReturn(callbackResponse)
            .when(callbackDispatcher)
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> actualResponse =
            asylumPreSubmitCallbackController.ccdAboutToStart(callback);

        assertNotNull(actualResponse);

        verify(callbackDispatcher, times(1)).handle(
            PreSubmitCallbackStage.ABOUT_TO_START,
            callback
        );
    }

    @Test
    public void should_dispatch_about_to_submit_callback_then_return_response() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetails));
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.empty());

        doReturn(callbackResponse)
            .when(callbackDispatcher)
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> actualResponse =
            asylumPreSubmitCallbackController.ccdAboutToSubmit(callback);

        assertNotNull(actualResponse);

        verify(callbackDispatcher, times(1)).handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );
    }

    @Test
    public void should_dispatch_about_to_submit_callback_then_return_response1() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.empty());
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.empty());

        doReturn(callbackResponse)
            .when(callbackDispatcher)
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> actualResponse =
            asylumPreSubmitCallbackController.ccdAboutToSubmit(callback);

        assertNotNull(actualResponse);

        verify(callbackDispatcher, times(1)).handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );
    }

    @Test
    public void should_not_allow_null_constructor_arguments() {

        assertThatThrownBy(() -> new AsylumPreSubmitCallbackController(null))
            .hasMessage("callbackDispatcher must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
