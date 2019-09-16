package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationIdAppender;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class CaseEditedNotifierTest {

    private static final String CASE_OFFICER_CASE_EDITED_TEMPLATE = "case-officer-template-id";

    @Mock private CaseOfficerCaseEditedNotifier caseOfficerCaseEditedNotifier;
    @Mock private NotificationSender notificationSender;
    @Mock private NotificationIdAppender notificationIdAppender;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private CaseEditedNotifier caseEditedNotifier;
    private Optional<CaseDetails<AsylumCase>> caseDetailsBefore;

    @Before
    public void setUp() {

        caseEditedNotifier =
            new CaseEditedNotifier(
                CASE_OFFICER_CASE_EDITED_TEMPLATE,
                caseOfficerCaseEditedNotifier,
                notificationSender,
                notificationIdAppender
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);

    }

    @Test
    public void should_handle_case_officer_email_and_personalisation() {

        caseEditedNotifier.handleCaseOfficer(callback, asylumCase, caseDetailsBefore);

        verify(caseOfficerCaseEditedNotifier, times(1)).getEmailAddress(asylumCase);
        verify(caseOfficerCaseEditedNotifier, times(1)).getPersonalisation(asylumCase, caseDetailsBefore);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> caseEditedNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        assertThatThrownBy(() -> caseEditedNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = caseEditedNotifier.canHandle(callbackStage, callback);

                if (event == Event.EDIT_CASE_LISTING
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

        assertThatThrownBy(() -> caseEditedNotifier.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseEditedNotifier.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseEditedNotifier.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseEditedNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
