package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS_SENT;

import java.util.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EndAppealPersonalisationFactory;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.GovNotifyTemplateIdConfiguration;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class EndAppealLegalRepresentativeNotifierTest extends BaseNotifierTest {

    private static final String END_APPEAL_LEGAL_REPRESENTATIVE_TEMPLATE = "end-appeal-legal-representative-template-id";

    @Mock private GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration;

    @Mock private EndAppealPersonalisationFactory endAppealPersonalisationFactory;

    private final String expectedNotificationReference = caseId + "_END_APPEAL_LEGAL_REPRESENTATIVE";

    private EndAppealNotifier endAppealNotifier;

    @Before
    public void setUp() {

        baseNotifierTestSetUp(Event.END_APPEAL);

        when(govNotifyTemplateIdConfiguration.getEndAppealLegalRepresentativeTemplateId()).thenReturn(END_APPEAL_LEGAL_REPRESENTATIVE_TEMPLATE);

        endAppealNotifier =
                new EndAppealNotifier(
                        govNotifyTemplateIdConfiguration,
                        HOME_OFFICE_EMAIL_ADDRESS,
                        notificationSender,
                        notificationIdAppender,
                        endAppealPersonalisationFactory
                );

        when(endAppealPersonalisationFactory.create(asylumCase)).thenReturn(personalisation);

        String legalRepEmailAddress = asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class).get();

        when(notificationSender.sendEmail(
                END_APPEAL_LEGAL_REPRESENTATIVE_TEMPLATE,
                legalRepEmailAddress,
                personalisation,
                expectedNotificationReference
        )).thenReturn(notificationId);
    }


    @Test
    public void should_send_end_appeal_email_notification_to_legal_rep() {

        final List<IdValue<String>> existingNotifications =
                new ArrayList<>(Collections.singletonList(
                        new IdValue<>("end-appeal-notification-send", "EEE-EEE-EEE-EEE")
                ));

        final List<IdValue<String>> expectedNotifications =
                new ArrayList<>(Arrays.asList(
                        new IdValue<>("end-appeal-notification-send", "EEE-EEE-EEE-EEE"),
                        new IdValue<>(expectedNotificationReference, notificationId)
                ));

        when(asylumCase.read(NOTIFICATIONS_SENT)).thenReturn(Optional.of(existingNotifications));

        when(notificationIdAppender.append(
                existingNotifications,
                expectedNotificationReference,
                notificationId
        )).thenReturn(expectedNotifications);

        String legalRepEmailAddress = asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class).get();

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                endAppealNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(notificationSender).sendEmail(
                END_APPEAL_LEGAL_REPRESENTATIVE_TEMPLATE,
                legalRepEmailAddress,
                personalisation,
                expectedNotificationReference
        );

        verify(asylumCase, times(1)).write(NOTIFICATIONS_SENT, expectedNotifications);
        verify(notificationIdAppender).append(existingNotifications, expectedNotificationReference, notificationId);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        handlingShouldThrowIfCannotActuallyHandle(endAppealNotifier);
    }

    @Test
    public void it_can_handle_callback() {

        itCanHandleCallback(endAppealNotifier, Event.END_APPEAL);
    }

    @Test
    public void should_not_allow_null_arguments() {

        shouldNotAllowNullArguments(endAppealNotifier);
    }
}
