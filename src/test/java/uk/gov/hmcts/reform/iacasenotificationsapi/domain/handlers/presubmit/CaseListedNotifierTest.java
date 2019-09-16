package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.util.Map;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.CaseOfficerPersonalisationFactory;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.HomeOfficePersonalisationFactory;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LegalRepresentativePersonalisationFactory;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationIdAppender;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class CaseListedNotifierTest {

    private static final String CASE_OFFICER_CASE_LISTED_TEMPLATE = "case-officer-template-id";
    private static final String LEGAL_REPRESENTATIVE_CASE_LISTED_TEMPLATE = "legal-representative-template-id";
    private static final String HOME_OFFICE_CASE_LISTED_TEMPLATE = "home-office-template-id";

    @Mock private CaseOfficerCaseListedNotifier caseOfficerCaseListedNotifier;
    @Mock private LegalRepresentativeCaseListedNotifier legalRepresentativeCaseListedNotifier;
    @Mock private HomeOfficeCaseListedNotifier homeOfficeCaseListedNotifier;
    @Mock private CaseOfficerPersonalisationFactory caseOfficerPersonalisationFactory;
    @Mock private LegalRepresentativePersonalisationFactory legalRepresentativePersonalisationFactory;
    @Mock private HomeOfficePersonalisationFactory homeOfficePersonalisationFactory;
    @Mock private Map<HearingCentre, String> hearingCentreEmailAddresses;
    @Mock private NotificationSender notificationSender;
    @Mock private NotificationIdAppender notificationIdAppender;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private CaseListedNotifier caseListedNotifier;

    @Before
    public void setUp() {

        caseListedNotifier =
            new CaseListedNotifier(
                CASE_OFFICER_CASE_LISTED_TEMPLATE,
                LEGAL_REPRESENTATIVE_CASE_LISTED_TEMPLATE,
                HOME_OFFICE_CASE_LISTED_TEMPLATE,
                caseOfficerCaseListedNotifier,
                legalRepresentativeCaseListedNotifier,
                homeOfficeCaseListedNotifier,
                caseOfficerPersonalisationFactory,
                legalRepresentativePersonalisationFactory,
                homeOfficePersonalisationFactory,
                hearingCentreEmailAddresses,
                notificationSender,
                notificationIdAppender
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
    }

    @Test
    public void should_handle_case_officer_email_and_personalisation() {

        caseListedNotifier.handleCaseOfficer(callback, asylumCase);

        verify(caseOfficerCaseListedNotifier, times(1)).getEmailAddress(asylumCase);
        verify(caseOfficerCaseListedNotifier, times(1)).getPersonalisation(asylumCase);
    }

    @Test
    public void should_handle_legal_representative_email_and_personalisation() {

        caseListedNotifier.handleLegalRepresentative(callback, asylumCase);

        verify(legalRepresentativeCaseListedNotifier, times(1)).getEmailAddress(asylumCase);
        verify(legalRepresentativeCaseListedNotifier, times(1)).getPersonalisation(asylumCase);
    }

    @Test
    public void should_handle_home_office_email_and_personalisation() {

        caseListedNotifier.handleHomeOffice(callback, asylumCase);

        verify(homeOfficeCaseListedNotifier, times(1)).getEmailAddress(asylumCase);
        verify(homeOfficeCaseListedNotifier, times(1)).getPersonalisation(asylumCase);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> caseListedNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        assertThatThrownBy(() -> caseListedNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = caseListedNotifier.canHandle(callbackStage, callback);

                if (event == Event.LIST_CASE
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

        assertThatThrownBy(() -> caseListedNotifier.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseListedNotifier.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseListedNotifier.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseListedNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
