package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS_SENT;

import java.util.Map;
import java.util.Optional;
import org.mockito.Mock;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationIdAppender;

@SuppressWarnings("unchecked")
public abstract class BaseNotifierTest {

    public static final String HOME_OFFICE_EMAIL_ADDRESS = "home-office@example.com";
    public static final String LEGAL_REP_EMAIL_ADDRESS = "legal-rep-firm@example.com";

    @Mock public NotificationSender notificationSender;
    @Mock public NotificationIdAppender notificationIdAppender;

    @Mock public Callback<AsylumCase> callback;
    @Mock public CaseDetails<AsylumCase> caseDetails;
    @Mock public AsylumCase asylumCase;
    @Mock public Map<String, String> personalisation;

    public final long caseId = 123L;

    public final String notificationId = "DEF-GHI-JKL-MNO";

    public void baseNotifierTestSetUp(Event currentEvent) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(currentEvent);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of(LEGAL_REP_EMAIL_ADDRESS));
        when(asylumCase.read(NOTIFICATIONS_SENT)).thenReturn(Optional.empty());
    }


    public void handlingShouldThrowIfCannotActuallyHandle(PreSubmitCallbackHandler<AsylumCase> preSubmitCallbackHandler) {

        assertThatThrownBy(() -> preSubmitCallbackHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> preSubmitCallbackHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }


    public void itCanHandleCallback(PreSubmitCallbackHandler<AsylumCase> preSubmitCallbackHandler, Event currentEvent) {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = preSubmitCallbackHandler.canHandle(callbackStage, callback);

                if (event == currentEvent && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }


    public void shouldNotAllowNullArguments(PreSubmitCallbackHandler<AsylumCase> preSubmitCallbackHandler) {

        assertThatThrownBy(() -> preSubmitCallbackHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> preSubmitCallbackHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

}
