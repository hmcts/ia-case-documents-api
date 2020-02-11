package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.function.BiPredicate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;


@RunWith(MockitoJUnitRunner.class)
public class NotificationHandlerTest {

    @Mock Callback<AsylumCase> callback;
    @Mock CaseDetails<AsylumCase> caseDetails;
    @Mock AsylumCase asylumCase;
    @Mock NotificationGenerator notificationGenerator;
    @Mock BiPredicate<PreSubmitCallbackStage, Callback<AsylumCase>> canHandle;

    private PreSubmitCallbackStage callbackStage = PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
    private NotificationHandler notificationHandler;

    @Before
    public void setup() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(canHandle.test(callbackStage, callback)).thenReturn(true);

        notificationHandler = new NotificationHandler(canHandle, Collections.singletonList(notificationGenerator));
    }

    @Test
    public void should_generate_notification_when_event_can_be_handled() {
        PreSubmitCallbackResponse<AsylumCase> response = notificationHandler.handle(callbackStage, callback);

        assertEquals(asylumCase, response.getData());
        verify(notificationGenerator).generate(callback);
    }

    @Test
    public void should_not_generate_notification_when_cannot_handle_event() {
        when(canHandle.test(callbackStage, callback)).thenReturn(false);

        assertThatThrownBy(() -> notificationHandler.handle(callbackStage, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");

        verifyZeroInteractions(notificationGenerator);
    }

    @Test
    public void should_return_false_when_cannot_handle_event() {
        when(canHandle.test(callbackStage, callback)).thenReturn(false);

        assertEquals(false, notificationHandler.canHandle(callbackStage, callback));
    }

    @Test
    public void should_throw_exception_when_callback_stage_is_null() {
        assertThatThrownBy(() -> notificationHandler.canHandle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");
    }

    @Test
    public void should_throw_exception_when_callback_is_null() {
        assertThatThrownBy(() -> notificationHandler.canHandle(callbackStage, null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }
}
