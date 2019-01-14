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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class BuildCaseDirectionNotifierTest {

    private static final String BUILD_CASE_DIRECTION_TEMPLATE = "template-id";
    private static final String CCD_URL = "http://ccd";

    @Mock private DirectionFinder directionFinder;
    @Mock private NotificationSender notificationSender;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Direction buildCaseDirection;

    @Captor private ArgumentCaptor<List<IdValue<String>>> existingNotificationsSentCaptor;

    final long caseId = 123L;

    final String legalRepresentativeEmailAddress = "legal-representative@example.com";
    final String legalRepReferenceNumber = "SOMETHING";
    final String appealReferenceNumber = "PA/001/2018";
    final String appellantGivenNames = "Jane";
    final String appellantFamilyName = "Doe";
    final String buildCaseDirectionExplanation = "Build your case";
    final String buildCaseDirectionDateDue = "2018-12-31";
    final String buildCaseDirectionDateDueFormatted = "31 Dec 2018";

    final Map<String, String> expectedPersonalisation =
        ImmutableMap
            .<String, String>builder()
            .put("Appeal Ref Number", appealReferenceNumber)
            .put("LR reference", legalRepReferenceNumber)
            .put("Given names", appellantGivenNames)
            .put("Family name", appellantFamilyName)
            .put("Hyperlink to user’s case list", CCD_URL)
            .put("Explanation", buildCaseDirectionExplanation)
            .put("due date", buildCaseDirectionDateDueFormatted)
            .build();

    final String expectedNotificationId = "ABC-DEF-GHI-JKL";
    final String expectedNotificationReference = caseId + "_BUILD_CASE_DIRECTION";

    private BuildCaseDirectionNotifier buildCaseDirectionNotifier;

    @Before
    public void setUp() {
        buildCaseDirectionNotifier =
            new BuildCaseDirectionNotifier(
                BUILD_CASE_DIRECTION_TEMPLATE,
                CCD_URL,
                directionFinder,
                notificationSender
            );
    }

    @Test
    public void should_send_build_case_direction_notification() {

        final List<IdValue<String>> existingNotifications =
            new ArrayList<>(Arrays.asList(
                new IdValue<>("some-notification-sent", "ZZZ-ZZZ-ZZZ-ZZZ")
            ));

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_RESPONDENT_EVIDENCE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.getLegalRepresentativeEmailAddress()).thenReturn(Optional.of(legalRepresentativeEmailAddress));
        when(asylumCase.getNotificationsSent()).thenReturn(Optional.of(existingNotifications));
        when(directionFinder.findFirst(asylumCase, DirectionTag.BUILD_CASE)).thenReturn(Optional.of(buildCaseDirection));

        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.getAppealReferenceNumber()).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.getLegalRepReferenceNumber()).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.getAppellantGivenNames()).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.getAppellantFamilyName()).thenReturn(Optional.of(appellantFamilyName));
        when(buildCaseDirection.getExplanation()).thenReturn(buildCaseDirectionExplanation);
        when(buildCaseDirection.getDateDue()).thenReturn(buildCaseDirectionDateDue);

        when(notificationSender.sendEmail(
            BUILD_CASE_DIRECTION_TEMPLATE,
            legalRepresentativeEmailAddress,
            expectedPersonalisation,
            expectedNotificationReference
        )).thenReturn(expectedNotificationId);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            buildCaseDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(notificationSender, times(1)).sendEmail(
            BUILD_CASE_DIRECTION_TEMPLATE,
            legalRepresentativeEmailAddress,
            expectedPersonalisation,
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

        assertEquals(caseId + "_BUILD_CASE_DIRECTION", actualExistingNotificationsSent.get(1).getId());
        assertEquals(expectedNotificationId, actualExistingNotificationsSent.get(1).getValue());
    }

    @Test
    public void should_send_build_case_direction_notification_when_no_notifications_exist() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_RESPONDENT_EVIDENCE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.getLegalRepresentativeEmailAddress()).thenReturn(Optional.of(legalRepresentativeEmailAddress));
        when(asylumCase.getNotificationsSent()).thenReturn(Optional.empty());
        when(directionFinder.findFirst(asylumCase, DirectionTag.BUILD_CASE)).thenReturn(Optional.of(buildCaseDirection));

        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.getAppealReferenceNumber()).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.getLegalRepReferenceNumber()).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.getAppellantGivenNames()).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.getAppellantFamilyName()).thenReturn(Optional.of(appellantFamilyName));
        when(buildCaseDirection.getExplanation()).thenReturn(buildCaseDirectionExplanation);
        when(buildCaseDirection.getDateDue()).thenReturn(buildCaseDirectionDateDue);

        when(notificationSender.sendEmail(
            BUILD_CASE_DIRECTION_TEMPLATE,
            legalRepresentativeEmailAddress,
            expectedPersonalisation,
            expectedNotificationReference
        )).thenReturn(expectedNotificationId);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            buildCaseDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(notificationSender, times(1)).sendEmail(
            BUILD_CASE_DIRECTION_TEMPLATE,
            legalRepresentativeEmailAddress,
            expectedPersonalisation,
            expectedNotificationReference
        );

        verify(asylumCase, times(1)).setNotificationsSent(existingNotificationsSentCaptor.capture());

        List<IdValue<String>> actualExistingNotificationsSent =
            existingNotificationsSentCaptor
                .getAllValues()
                .get(0);

        assertEquals(1, actualExistingNotificationsSent.size());

        assertEquals(caseId + "_BUILD_CASE_DIRECTION", actualExistingNotificationsSent.get(0).getId());
        assertEquals(expectedNotificationId, actualExistingNotificationsSent.get(0).getValue());
    }

    @Test
    public void should_send_build_case_direction_notification_using_defaults_where_available() {

        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", "")
                .put("LR reference", "")
                .put("Given names", "")
                .put("Family name", "")
                .put("Hyperlink to user’s case list", CCD_URL)
                .put("Explanation", buildCaseDirectionExplanation)
                .put("due date", buildCaseDirectionDateDueFormatted)
                .build();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_RESPONDENT_EVIDENCE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.getLegalRepresentativeEmailAddress()).thenReturn(Optional.of(legalRepresentativeEmailAddress));
        when(asylumCase.getNotificationsSent()).thenReturn(Optional.empty());
        when(directionFinder.findFirst(asylumCase, DirectionTag.BUILD_CASE)).thenReturn(Optional.of(buildCaseDirection));

        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.getAppealReferenceNumber()).thenReturn(Optional.empty());
        when(asylumCase.getLegalRepReferenceNumber()).thenReturn(Optional.empty());
        when(asylumCase.getAppellantGivenNames()).thenReturn(Optional.empty());
        when(asylumCase.getAppellantFamilyName()).thenReturn(Optional.empty());
        when(buildCaseDirection.getExplanation()).thenReturn(buildCaseDirectionExplanation);
        when(buildCaseDirection.getDateDue()).thenReturn(buildCaseDirectionDateDue);

        when(notificationSender.sendEmail(
            BUILD_CASE_DIRECTION_TEMPLATE,
            legalRepresentativeEmailAddress,
            expectedPersonalisation,
            expectedNotificationReference
        )).thenReturn(expectedNotificationId);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            buildCaseDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(notificationSender, times(1)).sendEmail(
            BUILD_CASE_DIRECTION_TEMPLATE,
            legalRepresentativeEmailAddress,
            expectedPersonalisation,
            expectedNotificationReference
        );

        verify(asylumCase, times(1)).setNotificationsSent(existingNotificationsSentCaptor.capture());

        List<IdValue<String>> actualExistingNotificationsSent =
            existingNotificationsSentCaptor
                .getAllValues()
                .get(0);

        assertEquals(1, actualExistingNotificationsSent.size());

        assertEquals(caseId + "_BUILD_CASE_DIRECTION", actualExistingNotificationsSent.get(0).getId());
        assertEquals(expectedNotificationId, actualExistingNotificationsSent.get(0).getValue());
    }

    @Test
    public void should_throw_when_build_case_direction_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_RESPONDENT_EVIDENCE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(directionFinder.findFirst(asylumCase, DirectionTag.BUILD_CASE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> buildCaseDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("build case direction is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_legal_representative_email_address_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_RESPONDENT_EVIDENCE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.getLegalRepresentativeEmailAddress()).thenReturn(Optional.empty());
        when(directionFinder.findFirst(asylumCase, DirectionTag.BUILD_CASE)).thenReturn(Optional.of(buildCaseDirection));

        assertThatThrownBy(() -> buildCaseDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("legalRepresentativeEmailAddress is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> buildCaseDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> buildCaseDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = buildCaseDirectionNotifier.canHandle(callbackStage, callback);

                if (event == Event.UPLOAD_RESPONDENT_EVIDENCE
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

        assertThatThrownBy(() -> buildCaseDirectionNotifier.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> buildCaseDirectionNotifier.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> buildCaseDirectionNotifier.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> buildCaseDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
