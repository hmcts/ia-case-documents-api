package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.RespondentDirectionPersonalisationFactory;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationIdAppender;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class RespondentReviewDirectionNotifierTest {

    private static final String RESPONDENT_REVIEW_DIRECTION_TEMPLATE = "template-id";

    @Mock private RespondentDirectionPersonalisationFactory respondentDirectionPersonalisationFactory;
    @Mock private DirectionFinder directionFinder;
    @Mock private NotificationSender notificationSender;
    @Mock private NotificationIdAppender notificationIdAppender;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Direction respondentReviewDirection;
    @Mock private Map<String, String> personalisation;

    private final long caseId = 123L;

    private final String respondentEmailAddress = "respondent@example.com";

    private final String expectedNotificationId = "ABC-DEF-GHI-JKL";
    private final String expectedNotificationReference = caseId + "_RESPONDENT_REVIEW_DIRECTION";

    private RespondentReviewDirectionNotifier respondentReviewDirectionNotifier;

    @Before
    public void setUp() {
        respondentReviewDirectionNotifier =
            new RespondentReviewDirectionNotifier(
                RESPONDENT_REVIEW_DIRECTION_TEMPLATE,
                respondentEmailAddress,
                respondentDirectionPersonalisationFactory,
                directionFinder,
                notificationSender,
                notificationIdAppender
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONDENT_REVIEW);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(directionFinder.findFirst(asylumCase, DirectionTag.RESPONDENT_REVIEW)).thenReturn(Optional.of(respondentReviewDirection));
        when(respondentDirectionPersonalisationFactory.create(asylumCase, respondentReviewDirection)).thenReturn(personalisation);

        when(caseDetails.getId()).thenReturn(caseId);

        when(notificationSender.sendEmail(
            RESPONDENT_REVIEW_DIRECTION_TEMPLATE,
            respondentEmailAddress,
            personalisation,
            expectedNotificationReference
        )).thenReturn(expectedNotificationId);
    }

    @Test
    public void should_send_respondent_review_direction_notification() {

        final List<IdValue<String>> existingNotifications =
            new ArrayList<>(Collections.singletonList(
                new IdValue<>("some-notification-sent", "ZZZ-ZZZ-ZZZ-ZZZ")
            ));

        final List<IdValue<String>> expectedNotifications =
            new ArrayList<>(Lists.newArrayList(
                new IdValue<>("some-notification-sent", "ZZZ-ZZZ-ZZZ-ZZZ"),
                new IdValue<>(expectedNotificationReference, expectedNotificationId)
            ));

        when(asylumCase.getNotificationsSent()).thenReturn(Optional.of(existingNotifications));

        when(notificationIdAppender.append(
            existingNotifications,
            expectedNotificationReference,
            expectedNotificationId))
            .thenReturn(expectedNotifications);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            respondentReviewDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(notificationSender, times(1)).sendEmail(
            RESPONDENT_REVIEW_DIRECTION_TEMPLATE,
            respondentEmailAddress,
            personalisation,
            expectedNotificationReference
        );

        verify(asylumCase, times(1)).setNotificationsSent(expectedNotifications);
        verify(notificationIdAppender).append(anyList(), anyString(), anyString());

    }

    @Test
    public void should_send_respondent_review_direction_notification_when_no_notifications_exist() {

        final List<IdValue<String>> expectedNotifications =
            new ArrayList<>(Lists.newArrayList(
                new IdValue<>(expectedNotificationReference, expectedNotificationId)
            ));

        when(asylumCase.getNotificationsSent()).thenReturn(Optional.empty());
        when(notificationIdAppender.append(
            Lists.emptyList(),
            expectedNotificationReference,
            expectedNotificationId))
            .thenReturn(expectedNotifications);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            respondentReviewDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(notificationSender, times(1)).sendEmail(
            RESPONDENT_REVIEW_DIRECTION_TEMPLATE,
            respondentEmailAddress,
            personalisation,
            expectedNotificationReference
        );

        verify(asylumCase, times(1)).setNotificationsSent(expectedNotifications);
        verify(notificationIdAppender).append(anyList(), anyString(), anyString());

    }

    @Test
    public void should_throw_when_respondent_review_direction_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONDENT_REVIEW);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(directionFinder.findFirst(asylumCase, DirectionTag.RESPONDENT_REVIEW)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> respondentReviewDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("direction 'respondentReview' is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> respondentReviewDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> respondentReviewDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = respondentReviewDirectionNotifier.canHandle(callbackStage, callback);

                if (event == Event.REQUEST_RESPONDENT_REVIEW
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

        assertThatThrownBy(() -> respondentReviewDirectionNotifier.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> respondentReviewDirectionNotifier.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> respondentReviewDirectionNotifier.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> respondentReviewDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
