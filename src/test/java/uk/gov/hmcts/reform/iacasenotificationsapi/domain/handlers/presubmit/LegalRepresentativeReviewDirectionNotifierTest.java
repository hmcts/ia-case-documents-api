package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LegalRepresentativePersonalisationFactory;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationIdAppender;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class LegalRepresentativeReviewDirectionNotifierTest {

    private static final String LEGAL_REPRESENTATIVE_REVIEW_TEMPLATE = "template-id";

    @Mock private LegalRepresentativePersonalisationFactory legalRepresentativePersonalisationFactory;
    @Mock private DirectionFinder directionFinder;
    @Mock private NotificationSender notificationSender;
    @Mock private NotificationIdAppender notificationIdAppender;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Direction legalRepresentativeReviewDirection;
    @Mock private Map<String, String> personalisation;

    @Captor private ArgumentCaptor<List<IdValue<String>>> existingNotificationsSentCaptor;

    private final long caseId = 123L;

    private final String legalRepresentativeEmailAddress = "legal-representative@example.com";

    private final String expectedNotificationId = "ABC-DEF-GHI-JKL";
    private final String expectedNotificationReference = caseId + "_LEGAL_REPRESENTATIVE_REVIEW_DIRECTION";

    private LegalRepresentativeReviewDirectionNotifier legalRepresentativeReviewDirectionNotifier;

    @Before
    public void setUp() {
        legalRepresentativeReviewDirectionNotifier =
            new LegalRepresentativeReviewDirectionNotifier(
                LEGAL_REPRESENTATIVE_REVIEW_TEMPLATE,
                legalRepresentativePersonalisationFactory,
                directionFinder,
                notificationSender,
                notificationIdAppender
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.ADD_APPEAL_RESPONSE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.getLegalRepresentativeEmailAddress()).thenReturn(Optional.of(legalRepresentativeEmailAddress));
        when(asylumCase.getNotificationsSent()).thenReturn(Optional.empty());

        when(directionFinder.findFirst(asylumCase, DirectionTag.LEGAL_REPRESENTATIVE_REVIEW)).thenReturn(Optional.of(legalRepresentativeReviewDirection));
        when(legalRepresentativePersonalisationFactory.create(asylumCase, legalRepresentativeReviewDirection)).thenReturn(personalisation);

        when(notificationSender.sendEmail(
            LEGAL_REPRESENTATIVE_REVIEW_TEMPLATE,
            legalRepresentativeEmailAddress,
            personalisation,
            expectedNotificationReference
        )).thenReturn(expectedNotificationId);
    }

    @Test
    public void should_send_legal_rep_review_direction_notification() {

        final List<IdValue<String>> existingNotifications =
            new ArrayList<>(Collections.singletonList(
                new IdValue<>("review-direction-notification-sent", "ZZZ-ZZZ-ZZZ-ZZZ")
            ));

        final List<IdValue<String>> expectedNotifications =
            new ArrayList<>(Lists.newArrayList(
                new IdValue<>("review-direction-notification-sent", "ZZZ-ZZZ-ZZZ-ZZZ"),
                new IdValue<>(expectedNotificationReference, expectedNotificationId)
            ));


        when(asylumCase.getNotificationsSent()).thenReturn(Optional.of(existingNotifications));
        when(notificationIdAppender.append(
            existingNotifications,
            expectedNotificationReference,
            expectedNotificationId
        )).thenReturn(expectedNotifications);


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            legalRepresentativeReviewDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(notificationSender, times(1)).sendEmail(
            LEGAL_REPRESENTATIVE_REVIEW_TEMPLATE,
            legalRepresentativeEmailAddress,
            personalisation,
            expectedNotificationReference
        );

        verify(asylumCase, times(1)).setNotificationsSent(expectedNotifications);
        verify(notificationIdAppender).append(anyList(), anyString(), anyString());

    }

    @Test
    public void should_send_legal_rep_review_direction_notification_when_no_notifications_exist() {

        final List<IdValue<String>> expectedNotifications =
            new ArrayList<>(Lists.newArrayList(
                new IdValue<>(expectedNotificationReference, expectedNotificationId)
            ));

        when(asylumCase.getNotificationsSent()).thenReturn(Optional.empty());
        when(notificationIdAppender.append(
            Lists.emptyList(),
            expectedNotificationReference,
            expectedNotificationId
        )).thenReturn(expectedNotifications);


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            legalRepresentativeReviewDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(notificationSender, times(1)).sendEmail(
            LEGAL_REPRESENTATIVE_REVIEW_TEMPLATE,
            legalRepresentativeEmailAddress,
            personalisation,
            expectedNotificationReference
        );

        verify(asylumCase, times(1)).setNotificationsSent(existingNotificationsSentCaptor.capture());

        List<IdValue<String>> actualExistingNotificationsSent =
            existingNotificationsSentCaptor
                .getAllValues()
                .get(0);

        assertEquals(1, actualExistingNotificationsSent.size());

        assertEquals(caseId + "_LEGAL_REPRESENTATIVE_REVIEW_DIRECTION", actualExistingNotificationsSent.get(0).getId());
        assertEquals(expectedNotificationId, actualExistingNotificationsSent.get(0).getValue());
    }

    @Test
    public void should_throw_when_legal_representative_review_direction_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.ADD_APPEAL_RESPONSE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(directionFinder.findFirst(asylumCase, DirectionTag.LEGAL_REPRESENTATIVE_REVIEW)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeReviewDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("legal representative review direction is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_legal_representative_email_address_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.ADD_APPEAL_RESPONSE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.getLegalRepresentativeEmailAddress()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeReviewDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("legalRepresentativeEmailAddress is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> legalRepresentativeReviewDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> legalRepresentativeReviewDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = legalRepresentativeReviewDirectionNotifier.canHandle(callbackStage, callback);

                if (event == Event.ADD_APPEAL_RESPONSE
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

        assertThatThrownBy(() -> legalRepresentativeReviewDirectionNotifier.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> legalRepresentativeReviewDirectionNotifier.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> legalRepresentativeReviewDirectionNotifier.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> legalRepresentativeReviewDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
