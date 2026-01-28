package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.NOTIFICATIONS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.StoredNotification;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.SaveNotificationsToDataPdfService;

@ExtendWith(MockitoExtension.class)
class SaveNotificationsToDataBailHandlerTest {

    @Mock
    private SaveNotificationsToDataPdfService saveNotificationsToDataPdfService;

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    @Mock
    private List<IdValue<StoredNotification>> mockNotificationList;
    @Mock
    private List<IdValue<StoredNotification>> mockChangedList;

    private SaveNotificationsToDataBailHandler saveNotificationsToDataBailHandler;

    @BeforeEach
    void setUp() {
        saveNotificationsToDataBailHandler =
            new SaveNotificationsToDataBailHandler(saveNotificationsToDataPdfService);
    }

    @Test
    void should_write_new_notification_data() {
        when(callback.getEvent()).thenReturn(Event.SAVE_NOTIFICATIONS_TO_DATA_BAIL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(NOTIFICATIONS)).thenReturn(Optional.of(mockNotificationList));
        when(saveNotificationsToDataPdfService.generatePdfsForNotifications(mockNotificationList))
            .thenReturn(mockChangedList);

        saveNotificationsToDataBailHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(bailCase, never()).write(NOTIFICATIONS, mockNotificationList);
        verify(bailCase, times(1)).write(NOTIFICATIONS, mockChangedList);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class)
    void it_can_handle_callback(Event event) {
        when(callback.getEvent()).thenReturn(event);
        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
            boolean canHandle = saveNotificationsToDataBailHandler.canHandle(callbackStage, callback);
            if (event == Event.SAVE_NOTIFICATIONS_TO_DATA_BAIL
                && callbackStage == ABOUT_TO_SUBMIT) {
                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }
        }
    }

    @Test
    void throws_if_cannot_handle_callback() {
        assertThatThrownBy(() -> saveNotificationsToDataBailHandler.handle(ABOUT_TO_START, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> saveNotificationsToDataBailHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> saveNotificationsToDataBailHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> saveNotificationsToDataBailHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> saveNotificationsToDataBailHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
