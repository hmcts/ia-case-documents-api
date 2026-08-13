package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.StoredNotification;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.SaveNotificationsToDataPdfService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
class SaveNotificationsToDataHandlerTest {

    @Mock
    private SaveNotificationsToDataPdfService saveNotificationsToDataPdfService;

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private List<IdValue<StoredNotification>> mockNotificationList;
    @Mock
    private List<IdValue<StoredNotification>> mockChangedList;

    private SaveNotificationsToDataHandler saveNotificationsToDataHandler;

    @BeforeEach
    void setUp() {
        saveNotificationsToDataHandler =
            new SaveNotificationsToDataHandler(saveNotificationsToDataPdfService);
    }

    @Test
    void should_write_new_notification_data() {
        when(callback.getEvent()).thenReturn(Event.SAVE_NOTIFICATIONS_TO_DATA);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(NOTIFICATIONS)).thenReturn(Optional.of(mockNotificationList));
        when(saveNotificationsToDataPdfService.generatePdfsForNotifications(mockNotificationList, asylumCase))
            .thenReturn(mockChangedList);

        saveNotificationsToDataHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, never()).write(NOTIFICATIONS, mockNotificationList);
        verify(asylumCase, times(1)).write(NOTIFICATIONS, mockChangedList);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class)
    void it_can_handle_callback(Event event) {
        when(callback.getEvent()).thenReturn(event);
        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
            boolean canHandle = saveNotificationsToDataHandler.canHandle(callbackStage, callback);
            if (event == Event.SAVE_NOTIFICATIONS_TO_DATA
                && callbackStage == ABOUT_TO_SUBMIT) {
                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }
        }
    }

    @Test
    void throws_if_cannot_handle_callback() {
        assertThatThrownBy(() -> saveNotificationsToDataHandler.handle(ABOUT_TO_START, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> saveNotificationsToDataHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> saveNotificationsToDataHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> saveNotificationsToDataHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> saveNotificationsToDataHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
