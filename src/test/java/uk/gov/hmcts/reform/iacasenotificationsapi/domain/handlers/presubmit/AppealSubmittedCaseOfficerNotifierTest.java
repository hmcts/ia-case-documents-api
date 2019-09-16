package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS_SENT;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationIdAppender;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class AppealSubmittedCaseOfficerNotifierTest {

    private static final String APPEAL_SUBMITTED_CASE_OFFICER_TEMPLATE = "template-id";
    private static final String BRADFORD_EMAIL_ADDRESS = "bradford@example.com";
    private static final String MANCHESTER_EMAIL_ADDRESS = "manchester@example.com";
    private static final String NEWPORT_EMAIL_ADDRESS = "newport@example.com";
    private static final String TAYLOR_HOUSE_EMAIL_ADDRESS = "taylorHouse@example.com";

    @Mock private CaseOfficerPersonalisationFactory caseOfficerPersonalisationFactory;
    @Mock private Map<HearingCentre, String> hearingCentreEmailAddresses;
    @Mock private NotificationSender notificationSender;
    @Mock private NotificationIdAppender notificationIdAppender;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Map<String, String> personalisation;

    private final long caseId = 123L;

    private final String expectedNotificationId = "ABC-DEF-GHI-JKL";
    private final String expectedNotificationReference = caseId + "_APPEAL_SUBMITTED_CASE_OFFICER";

    private AppealSubmittedCaseOfficerNotifier appealSubmittedCaseOfficerNotifier;

    @Before
    public void setUp() {
        appealSubmittedCaseOfficerNotifier =
            new AppealSubmittedCaseOfficerNotifier(
                APPEAL_SUBMITTED_CASE_OFFICER_TEMPLATE,
                caseOfficerPersonalisationFactory,
                hearingCentreEmailAddresses,
                notificationSender,
                notificationIdAppender
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.read(NOTIFICATIONS_SENT)).thenReturn(Optional.empty());

        when(caseOfficerPersonalisationFactory.create(asylumCase)).thenReturn(personalisation);

        when(hearingCentreEmailAddresses.get(HearingCentre.BRADFORD)).thenReturn(BRADFORD_EMAIL_ADDRESS);
        when(hearingCentreEmailAddresses.get(HearingCentre.MANCHESTER)).thenReturn(MANCHESTER_EMAIL_ADDRESS);
        when(hearingCentreEmailAddresses.get(HearingCentre.NEWPORT)).thenReturn(NEWPORT_EMAIL_ADDRESS);
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
            new ArrayList<>(Collections.singletonList(
                new IdValue<>("appeal-submitted-notification-sent", "ZZZ-ZZZ-ZZZ-ZZZ")
            ));

        final List<IdValue<String>> expectedNotifications =
            new ArrayList<>(Arrays.asList(
                new IdValue<>("appeal-submitted-notification-sent", "ZZZ-ZZZ-ZZZ-ZZZ"),
                new IdValue<>(expectedNotificationReference, expectedNotificationId)
            ));

        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(asylumCase.read(NOTIFICATIONS_SENT)).thenReturn(Optional.of(existingNotifications));

        when(notificationIdAppender.append(
            existingNotifications,
            expectedNotificationReference,
            expectedNotificationId
        )).thenReturn(expectedNotifications);

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

        verify(asylumCase, times(1)).write(NOTIFICATIONS_SENT, expectedNotifications);
        verify(notificationIdAppender).append(anyList(), anyString(), anyString());

    }

    @Test
    public void should_send_appeal_submitted_notification_to_hearing_centre_when_no_existing_notifications_exist() {

        final List<IdValue<String>> expectedNotifications =
            new ArrayList<>(Collections.singletonList(
                new IdValue<>(expectedNotificationReference, expectedNotificationId)
            ));

        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCase.read(NOTIFICATIONS_SENT)).thenReturn(Optional.empty());
        when(notificationIdAppender.append(
            Lists.emptyList(),
            expectedNotificationReference,
            expectedNotificationId
        )).thenReturn(expectedNotifications);


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

        verify(asylumCase, times(1)).write(NOTIFICATIONS_SENT, expectedNotifications);

    }

    @Test
    public void should_use_email_for_hearing_centre() {

        Map<HearingCentre, String> exampleInputOutputs =
            ImmutableMap.<HearingCentre, String>builder()
                .put(HearingCentre.BRADFORD, BRADFORD_EMAIL_ADDRESS)
                .put(HearingCentre.MANCHESTER, MANCHESTER_EMAIL_ADDRESS)
                .put(HearingCentre.NEWPORT, NEWPORT_EMAIL_ADDRESS)
                .put(HearingCentre.TAYLOR_HOUSE, TAYLOR_HOUSE_EMAIL_ADDRESS)
                .build();

        exampleInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final HearingCentre inputHearingCentre = inputOutput.getKey();
                final String outputEmailAddress = inputOutput.getValue();

                when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(inputHearingCentre));

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
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appealSubmittedCaseOfficerNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("hearingCentre is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_hearing_centre_email_address_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.MANCHESTER));
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
