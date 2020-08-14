package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative.LegalRepresentativeEditListingNoChangePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative.LegalRepresentativeEditListingPersonalisation;

@RunWith(MockitoJUnitRunner.class)
public class EditListingEmailNotificationGeneratorTest {

    @Mock
    LegalRepresentativeEditListingNoChangePersonalisation editListingNoChangeEmailNotificationPersonalisation;
    @Mock
    LegalRepresentativeEditListingPersonalisation editListingChangeEmailNotificationPersonalisation1;

    @Mock NotificationSender notificationSender;
    @Spy NotificationIdAppender notificationIdAppender;
    @Mock Callback<AsylumCase> callback;
    @Mock CaseDetails<AsylumCase> caseDetails;
    @Mock CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock AsylumCase asylumCase;
    @Mock AsylumCase asylumCaseBefore;

    private List<EmailNotificationPersonalisation> repEmailNotificationPersonalisationList;

    private NotificationGenerator notificationGenerator;

    private Long caseId = 12345L;

    private String templateId1 = "templateId1";
    private String templateId2 = "templateId1";

    private String refId1 = "refId1";
    private String refId2 = "refId2";

    private String emailAddress1 = "email1@example.com";
    private String emailAddress2 = "email2@example.com";

    private Map<String, String> personalizationMap1 = emptyMap();
    private Map<String, String> personalizationMap2 = emptyMap();

    private List<IdValue<String>> notificationsSent = newArrayList();

    private String notificationId1 = "notificationId1";
    private String notificationId2 = "notificationId2";

    @Before
    public void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);
        when(caseDetails.getId()).thenReturn(caseId);

        when(asylumCase.read(AsylumCaseDefinition.NOTIFICATIONS_SENT)).thenReturn(Optional.of(notificationsSent));

        when(editListingNoChangeEmailNotificationPersonalisation.getReferenceId(caseId)).thenReturn(refId1);
        when(editListingChangeEmailNotificationPersonalisation1.getReferenceId(caseId)).thenReturn(refId2);

        when(editListingNoChangeEmailNotificationPersonalisation.getTemplateId()).thenReturn(templateId1);
        when(editListingChangeEmailNotificationPersonalisation1.getTemplateId()).thenReturn(templateId2);

        when(editListingNoChangeEmailNotificationPersonalisation.getPersonalisation(callback)).thenReturn(personalizationMap1);
        when(editListingChangeEmailNotificationPersonalisation1.getPersonalisation(callback)).thenReturn(personalizationMap2);

        when(notificationSender.sendEmail(templateId1, emailAddress1, personalizationMap1, refId1)).thenReturn(notificationId1);
        when(notificationSender.sendEmail(templateId2, emailAddress2, personalizationMap2, refId2)).thenReturn(notificationId2);

        when(notificationIdAppender.append(notificationsSent, refId1, notificationId1)).thenReturn(notificationsSent);
        when(notificationIdAppender.append(notificationsSent, refId2, notificationId2)).thenReturn(notificationsSent);

        repEmailNotificationPersonalisationList = newArrayList(editListingNoChangeEmailNotificationPersonalisation, editListingChangeEmailNotificationPersonalisation1);

    }

    @Test
    public void should_send_cno_hange_notification_when_edit_listing_is_unchanged() {
        notificationGenerator = new EditListingEmailNotificationGenerator(repEmailNotificationPersonalisationList, notificationSender, notificationIdAppender);

        when(editListingNoChangeEmailNotificationPersonalisation.getRecipientsList(asylumCase)).thenReturn(singleton(emailAddress1));

        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCaseBefore.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

        final String listingDateTime = "2020-02-06T13:51:29.369";
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(listingDateTime));
        when(asylumCaseBefore.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(listingDateTime));

        notificationGenerator.generate(callback);

        verify(notificationSender).sendEmail(templateId1, emailAddress1, personalizationMap1, refId1);
        verify(notificationSender, never()).sendEmail(templateId2, emailAddress2, personalizationMap2, refId2);

        verify(notificationIdAppender).appendAll(asylumCase, refId1, Collections.singletonList(notificationId1));
        verify(notificationIdAppender).append(notificationsSent, refId1, notificationId1);
        verify(notificationIdAppender, never()).appendAll(asylumCase, refId2, Collections.singletonList(notificationId2));
        verify(notificationIdAppender, never()).append(notificationsSent, refId2, notificationId2);

        verify(asylumCase, times(1)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_send_change_notification_when_edit_listing_hearing_centre_is_changed() {
        notificationGenerator = new EditListingEmailNotificationGenerator(repEmailNotificationPersonalisationList, notificationSender, notificationIdAppender);

        when(editListingChangeEmailNotificationPersonalisation1.getRecipientsList(asylumCase)).thenReturn(singleton(emailAddress2));

        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCaseBefore.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.MANCHESTER));

        final String listingDateTime = "2020-02-06T13:51:29.369";
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(listingDateTime));
        when(asylumCaseBefore.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(listingDateTime));

        notificationGenerator.generate(callback);

        verify(notificationSender, never()).sendEmail(templateId1, emailAddress1, personalizationMap1, refId1);
        verify(notificationSender).sendEmail(templateId2, emailAddress2, personalizationMap2, refId2);

        verify(notificationIdAppender, never()).appendAll(asylumCase, refId1, Collections.singletonList(notificationId1));
        verify(notificationIdAppender, never()).append(notificationsSent, refId1, notificationId1);
        verify(notificationIdAppender).appendAll(asylumCase, refId2, Collections.singletonList(notificationId2));
        verify(notificationIdAppender).append(notificationsSent, refId2, notificationId2);

        verify(asylumCase, times(1)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_send_change_notification_when_edit_listing_hearing_date_is_changed() {
        notificationGenerator = new EditListingEmailNotificationGenerator(repEmailNotificationPersonalisationList, notificationSender, notificationIdAppender);

        when(editListingChangeEmailNotificationPersonalisation1.getRecipientsList(asylumCase)).thenReturn(singleton(emailAddress2));

        final String listingDateTime = "2020-02-06T13:51:29.369";
        final String listingDateTimeBefore = "2020-02-04T13:51:29.369";
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(listingDateTime));
        when(asylumCaseBefore.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(listingDateTimeBefore));

        notificationGenerator.generate(callback);

        verify(notificationSender, never()).sendEmail(templateId1, emailAddress1, personalizationMap1, refId1);
        verify(notificationSender).sendEmail(templateId2, emailAddress2, personalizationMap2, refId2);

        verify(notificationIdAppender, never()).appendAll(asylumCase, refId1, Collections.singletonList(notificationId1));
        verify(notificationIdAppender, never()).append(notificationsSent, refId1, notificationId1);
        verify(notificationIdAppender).appendAll(asylumCase, refId2, Collections.singletonList(notificationId2));
        verify(notificationIdAppender).append(notificationsSent, refId2, notificationId2);

        verify(asylumCase, times(1)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_send_change_notification_when_edit_listing_hearing_date_time_is_changed() {
        notificationGenerator = new EditListingEmailNotificationGenerator(repEmailNotificationPersonalisationList, notificationSender, notificationIdAppender);

        when(editListingChangeEmailNotificationPersonalisation1.getRecipientsList(asylumCase)).thenReturn(singleton(emailAddress2));

        final String listingDateTime = "2020-02-06T13:51:29.111";
        final String listingDateTimeBefore = "2020-02-06T13:51:39.999";
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(listingDateTime));
        when(asylumCaseBefore.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(listingDateTimeBefore));

        notificationGenerator.generate(callback);

        verify(notificationSender, never()).sendEmail(templateId1, emailAddress1, personalizationMap1, refId1);
        verify(notificationSender).sendEmail(templateId2, emailAddress2, personalizationMap2, refId2);

        verify(notificationIdAppender, never()).appendAll(asylumCase, refId1, Collections.singletonList(notificationId1));
        verify(notificationIdAppender, never()).append(notificationsSent, refId1, notificationId1);
        verify(notificationIdAppender).appendAll(asylumCase, refId2, Collections.singletonList(notificationId2));
        verify(notificationIdAppender).append(notificationsSent, refId2, notificationId2);

        verify(asylumCase, times(1)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_not_send_notification_when_email_personalisation_list_empty() {
        notificationGenerator = new EmailNotificationGenerator(emptyList(), notificationSender, notificationIdAppender);
        notificationGenerator.generate(callback);

        verifyNoInteractions(notificationSender);
        verifyNoInteractions(notificationIdAppender);

        verify(asylumCase, times(0)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }

}
