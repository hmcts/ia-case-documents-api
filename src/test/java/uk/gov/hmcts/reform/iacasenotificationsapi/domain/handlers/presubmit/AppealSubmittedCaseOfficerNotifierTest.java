package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.CaseOfficerPersonalisationFactory;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class AppealSubmittedCaseOfficerNotifierTest {

    private static final String APPEAL_SUBMITTED_CASE_OFFICER_TEMPLATE = "template-id";
    private static final String MANCHESTER_EMAIL_ADDRESS = "manchester@example.com";
    private static final String TAYLOR_HOUSE_EMAIL_ADDRESS = "taylorHouse@example.com";

    @Mock private CaseOfficerPersonalisationFactory caseOfficerPersonalisationFactory;
    @Mock private Map<HearingCentre, String> hearingCentreEmailAddresses;
    @Mock private NotificationSender notificationSender;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Map<String, String> personalisation;

    @Captor private ArgumentCaptor<List<IdValue<String>>> existingNotificationsSentCaptor;

    final long caseId = 123L;

    final String expectedNotificationId = "ABC-DEF-GHI-JKL";
    final String expectedNotificationReference = caseId + "_APPEAL_SUBMITTED_CASE_OFFICER";

    private AppealSubmittedCaseOfficerNotifier appealSubmittedCaseOfficerNotifier;

    @Before
    public void setUp() {
        appealSubmittedCaseOfficerNotifier =
            new AppealSubmittedCaseOfficerNotifier(
                APPEAL_SUBMITTED_CASE_OFFICER_TEMPLATE,
                caseOfficerPersonalisationFactory,
                hearingCentreEmailAddresses,
                notificationSender
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.getNotificationsSent()).thenReturn(Optional.empty());

        when(caseOfficerPersonalisationFactory.create(asylumCase)).thenReturn(personalisation);

        when(hearingCentreEmailAddresses.get(HearingCentre.MANCHESTER)).thenReturn(MANCHESTER_EMAIL_ADDRESS);
        when(hearingCentreEmailAddresses.get(HearingCentre.TAYLOR_HOUSE)).thenReturn(TAYLOR_HOUSE_EMAIL_ADDRESS);

        when(notificationSender.sendEmail(
            eq(APPEAL_SUBMITTED_CASE_OFFICER_TEMPLATE),
            any(),
            eq(personalisation),
            any()
        )).thenReturn(expectedNotificationId);
    }

    @Test
    public void should_send_appeal_submitted_notification_to_hearing_centre() {

        final List<IdValue<String>> existingNotifications =
            new ArrayList<>(Arrays.asList(
                new IdValue<>("some-notification-sent", "ZZZ-ZZZ-ZZZ-ZZZ")
            ));

        when(asylumCase.getHearingCentre()).thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(asylumCase.getNotificationsSent()).thenReturn(Optional.of(existingNotifications));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealSubmittedCaseOfficerNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(notificationSender, times(1)).sendEmail(
            APPEAL_SUBMITTED_CASE_OFFICER_TEMPLATE,
            MANCHESTER_EMAIL_ADDRESS,
            personalisation,
            expectedNotificationReference
        );

        verify(asylumCase, times(1)).setNotificationsSent(existingNotificationsSentCaptor.capture());

        List<IdValue<String>> actualExistingNotificationsSent =
            existingNotificationsSentCaptor
                .getAllValues()
                .get(0);

        assertEquals(2, actualExistingNotificationsSent.size());

        assertEquals("some-notification-sent", actualExistingNotificationsSent.get(0).getId());
        assertEquals("ZZZ-ZZZ-ZZZ-ZZZ", actualExistingNotificationsSent.get(0).getValue());

        assertEquals(caseId + "_APPEAL_SUBMITTED_CASE_OFFICER", actualExistingNotificationsSent.get(1).getId());
        assertEquals(expectedNotificationId, actualExistingNotificationsSent.get(1).getValue());
    }

    @Test
    public void should_send_appeal_submitted_notification_to_hearing_centre_when_no_notifications_exist() {

        when(asylumCase.getHearingCentre()).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCase.getNotificationsSent()).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealSubmittedCaseOfficerNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(notificationSender, times(1)).sendEmail(
            APPEAL_SUBMITTED_CASE_OFFICER_TEMPLATE,
            TAYLOR_HOUSE_EMAIL_ADDRESS,
            personalisation,
            expectedNotificationReference
        );

        verify(asylumCase, times(1)).setNotificationsSent(existingNotificationsSentCaptor.capture());

        List<IdValue<String>> actualExistingNotificationsSent =
            existingNotificationsSentCaptor
                .getAllValues()
                .get(0);

        assertEquals(1, actualExistingNotificationsSent.size());

        assertEquals(caseId + "_APPEAL_SUBMITTED_CASE_OFFICER", actualExistingNotificationsSent.get(0).getId());
        assertEquals(expectedNotificationId, actualExistingNotificationsSent.get(0).getValue());
    }

    @Test
    public void should_use_email_for_hearing_centre() {

        Map<HearingCentre, String> exampleInputOutputs =
            ImmutableMap.<HearingCentre, String>builder()
                .put(HearingCentre.MANCHESTER, MANCHESTER_EMAIL_ADDRESS)
                .put(HearingCentre.TAYLOR_HOUSE, TAYLOR_HOUSE_EMAIL_ADDRESS)
                .build();

        exampleInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final HearingCentre inputHearingCentre = inputOutput.getKey();
                final String outputEmailAddress = inputOutput.getValue();

                when(asylumCase.getHearingCentre()).thenReturn(Optional.of(inputHearingCentre));

                appealSubmittedCaseOfficerNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

                verify(notificationSender, times(1)).sendEmail(
                    APPEAL_SUBMITTED_CASE_OFFICER_TEMPLATE,
                    outputEmailAddress,
                    personalisation,
                    expectedNotificationReference
                );

                reset(asylumCase);
            });
    }

    @Test
    public void should_throw_when_hearing_centre_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.getHearingCentre()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appealSubmittedCaseOfficerNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("hearingCentre is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_hearing_centre_email_address_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.getHearingCentre()).thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(hearingCentreEmailAddresses.get(HearingCentre.MANCHESTER)).thenReturn(null);

        assertThatThrownBy(() -> appealSubmittedCaseOfficerNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Hearing centre email address not found: manchester")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> appealSubmittedCaseOfficerNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.UPLOAD_RESPONDENT_EVIDENCE);
        assertThatThrownBy(() -> appealSubmittedCaseOfficerNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = appealSubmittedCaseOfficerNotifier.canHandle(callbackStage, callback);

                if (event == Event.SUBMIT_APPEAL
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> appealSubmittedCaseOfficerNotifier.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealSubmittedCaseOfficerNotifier.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealSubmittedCaseOfficerNotifier.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealSubmittedCaseOfficerNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
