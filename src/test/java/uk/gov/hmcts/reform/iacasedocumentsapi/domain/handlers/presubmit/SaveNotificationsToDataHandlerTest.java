package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.StoredNotification;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.SaveNotificationsToDataPdfService;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
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
    private Document document;
    @Mock
    private StoredNotification mockedStoredNotification;

    private final String reference = "someReference";
    private final String notificationId = "someNotificationId";
    private final String body = "someBody";
    private final String notificationType = "someNotificationType";
    private final String status = "someStatus";
    private final String email = "some-email@test.com";

    private SaveNotificationsToDataHandler saveNotificationsToDataHandler;

    @BeforeEach
    void setUp() {
        saveNotificationsToDataHandler =
            new SaveNotificationsToDataHandler(saveNotificationsToDataPdfService);
    }

    @Test
    void should_write_empty_list_if_no_stored_notifications() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SAVE_NOTIFICATIONS_TO_DATA);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> response =
            saveNotificationsToDataHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());

        verify(asylumCase, times(1))
            .write(NOTIFICATIONS, emptyList());
    }

    @Test
    void should_not_change_notification_if_document_not_null() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SAVE_NOTIFICATIONS_TO_DATA);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        StoredNotification storedNotification =
            new StoredNotification(notificationId, "2024-01-01", email,
                "<div>" + body + "</div>", notificationType, status, reference);
        storedNotification.setNotificationDocument(document);
        List<IdValue<StoredNotification>> storedNotifications =
            List.of(new IdValue<>(reference, storedNotification));
        when(asylumCase.read(NOTIFICATIONS)).thenReturn(Optional.of(storedNotifications));

        PreSubmitCallbackResponse<AsylumCase> response =
            saveNotificationsToDataHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());

        verify(asylumCase, times(1))
            .write(NOTIFICATIONS, storedNotifications);
    }

    @ParameterizedTest
    @ValueSource(strings = {"cancelled", "failed", "technical-failure",
        "temporary-failure", "permanent-failure", "validation-failed", "virus-scan-failed"})
    void should_not_change_notification_if_status_invalid(String invalidStatus) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SAVE_NOTIFICATIONS_TO_DATA);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        StoredNotification storedNotification =
            new StoredNotification(notificationId, "2024-01-01", email,
                "<div>" + body + "</div>",
                notificationType, invalidStatus, reference);
        List<IdValue<StoredNotification>> storedNotifications =
            List.of(new IdValue<>(reference, storedNotification));
        when(asylumCase.read(NOTIFICATIONS)).thenReturn(Optional.of(storedNotifications));

        PreSubmitCallbackResponse<AsylumCase> response =
            saveNotificationsToDataHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());

        verify(asylumCase, times(1))
            .write(NOTIFICATIONS, storedNotifications);
    }

    @Test
    void should_set_notification_document_if_valid() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SAVE_NOTIFICATIONS_TO_DATA);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        List<IdValue<StoredNotification>> storedNotifications =
            List.of(new IdValue<>(reference, mockedStoredNotification));
        when(asylumCase.read(NOTIFICATIONS)).thenReturn(Optional.of(storedNotifications));
        when(mockedStoredNotification.getNotificationStatus()).thenReturn(status);
        when(mockedStoredNotification.getNotificationBody()).thenReturn(body);
        when(mockedStoredNotification.getNotificationReference()).thenReturn(reference);
        when(saveNotificationsToDataPdfService.createPdf(body, reference)).thenReturn(document);
        PreSubmitCallbackResponse<AsylumCase> response =
            saveNotificationsToDataHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());
        verify(mockedStoredNotification, times(1)).setNotificationDocument(document);
    }

    @Test
    void should_generate_and_add_document_to_notification() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SAVE_NOTIFICATIONS_TO_DATA);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        StoredNotification storedNotification =
            new StoredNotification(notificationId, "2024-01-01", email,
                "<div>" + body + "</div>", notificationType, status, reference);
        List<IdValue<StoredNotification>> storedNotifications =
            List.of(new IdValue<>(reference, storedNotification));
        when(asylumCase.read(NOTIFICATIONS)).thenReturn(Optional.of(storedNotifications));
        when(saveNotificationsToDataPdfService.createPdf("<div>" + body + "</div>", reference))
            .thenReturn(document);
        PreSubmitCallbackResponse<AsylumCase> response =
            saveNotificationsToDataHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());
        storedNotification.setNotificationDocument(document);
        List<StoredNotification> expectedNotifications = List.of(storedNotification);
        Optional<List<IdValue<StoredNotification>>> readNotifications =
            response.getData().read(NOTIFICATIONS);
        List<StoredNotification> actualNotifications =
            readNotifications.orElse(emptyList()).stream().map((IdValue::getValue)).toList();
        assertEquals(expectedNotifications, actualNotifications);
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
