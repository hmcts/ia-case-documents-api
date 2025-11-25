package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_BUNDLE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.EmailAddressFinder.NO_EMAIL_ADDRESS_DECISION_WITHOUT_HEARING;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.ApplicationContextProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.GovNotifyNotificationSender;
import uk.gov.service.notify.NotificationClientException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class NotificationGeneratorTest {

    @Mock
    EmailNotificationPersonalisation emailNotificationPersonalisation;
    @Mock
    EmailNotificationPersonalisation emailNotificationPersonalisation1;
    @Mock
    SmsNotificationPersonalisation smsNotificationPersonalisation1;
    @Mock
    SmsNotificationPersonalisation smsNotificationPersonalisation2;
    @Mock
    LetterNotificationPersonalisation letterNotificationPersonalisation1;
    @Mock
    LetterNotificationPersonalisation letterNotificationPersonalisation2;
    @Mock
    GovNotifyNotificationSender notificationSender;
    @Spy
    NotificationIdAppender notificationIdAppender;
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    MockedStatic<ApplicationContextProvider> mocked;
    @Mock
    static ApplicationContext applicationContext;
    @Mock
    static CustomerServicesProvider customerServicesProvider;
    @Mock
    DocumentDownloadClient documentDownloadClient;
    @Mock
    Resource resource;
    @Mock
    InputStream mockInputStream;

    private List<EmailNotificationPersonalisation> repEmailNotificationPersonalisationList;
    private List<EmailNotificationPersonalisation> aipEmailNotificationPersonalisationList;
    private List<SmsNotificationPersonalisation> aipSmsNotificationPersonalisationList;
    private List<LetterNotificationPersonalisation> letterNotificationPersonalisationList;
    private List<DocumentTag> documentTagList;

    private NotificationGenerator notificationGenerator;

    private Long caseId = 12345L;

    private String templateId1 = "templateId1";
    private String templateId2 = "templateId1";

    private String refId1 = "refId1";
    private String refId2 = "refId2";
    private String emailAddress1 = "email1@example.com";
    private String emailAddress2 = "email2@example.com";

    private String phoneNumber1 = "07123456789";
    private String phoneNumber2 = "07123456780";

    private String address1 = "20_realstreet_London";
    private String address2 = "80_realstreet_London";

    private Map<String, String> personalizationMap1 = emptyMap();
    private Map<String, String> personalizationMap2 = emptyMap();

    private List<IdValue<String>> notificationsSent = newArrayList();

    private String notificationId1 = "notificationId1";
    private String notificationId2 = "notificationId2";
    private String documentBinaryUrl = "http://host:8080/a/b/c";

    private DocumentTag documentTag = DocumentTag.END_APPEAL;

    Document document = new Document(documentBinaryUrl, documentBinaryUrl, "end-appeal-notice");
    DocumentWithMetadata documentWithMetadata = new DocumentWithMetadata(document, "desc", null, documentTag);
    IdValue<DocumentWithMetadata> documentWithMetadataId = new IdValue<>("1", documentWithMetadata);
    private String refId3 = caseId + "_" + documentTag.name();


    @BeforeEach
    public void setup() throws NotificationClientException, IOException {
        mocked = mockStatic(ApplicationContextProvider.class);
        mocked.when(ApplicationContextProvider::getApplicationContext).thenReturn(applicationContext);
        when(applicationContext.getBean(CustomerServicesProvider.class)).thenReturn(customerServicesProvider);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);

        when(asylumCase.read(AsylumCaseDefinition.NOTIFICATIONS_SENT)).thenReturn(Optional.of(notificationsSent));

        when(emailNotificationPersonalisation.getReferenceId(caseId)).thenReturn(refId1);
        when(emailNotificationPersonalisation1.getReferenceId(caseId)).thenReturn(refId2);

        when(emailNotificationPersonalisation.getTemplateId()).thenReturn(templateId1);
        when(emailNotificationPersonalisation1.getTemplateId()).thenReturn(templateId2);

        when(emailNotificationPersonalisation.getPersonalisation(callback)).thenReturn(personalizationMap1);
        when(emailNotificationPersonalisation1.getPersonalisation(callback)).thenReturn(personalizationMap2);

        when(notificationSender.sendEmail(templateId1, emailAddress1, personalizationMap1, refId1, callback))
            .thenReturn(notificationId1);
        when(notificationSender.sendEmail(templateId2, emailAddress2, personalizationMap2, refId2, callback))
            .thenReturn(notificationId2);

        when(smsNotificationPersonalisation1.getReferenceId(caseId)).thenReturn(refId1);
        when(smsNotificationPersonalisation2.getReferenceId(caseId)).thenReturn(refId2);

        when(smsNotificationPersonalisation1.getTemplateId()).thenReturn(templateId1);
        when(smsNotificationPersonalisation2.getTemplateId()).thenReturn(templateId2);

        when(smsNotificationPersonalisation1.getPersonalisation(callback)).thenReturn(personalizationMap1);
        when(smsNotificationPersonalisation2.getPersonalisation(callback)).thenReturn(personalizationMap2);

        when(notificationSender.sendSms(templateId1, phoneNumber1, personalizationMap1, refId1, callback))
            .thenReturn(notificationId1);
        when(notificationSender.sendSms(templateId2, phoneNumber2, personalizationMap2, refId2, callback))
            .thenReturn(notificationId2);

        when(letterNotificationPersonalisation1.getReferenceId(caseId)).thenReturn(refId1);
        when(letterNotificationPersonalisation2.getReferenceId(caseId)).thenReturn(refId2);

        when(letterNotificationPersonalisation1.getTemplateId()).thenReturn(templateId1);
        when(letterNotificationPersonalisation2.getTemplateId()).thenReturn(templateId2);

        when(letterNotificationPersonalisation1.getPersonalisation(callback)).thenReturn(personalizationMap1);
        when(letterNotificationPersonalisation2.getPersonalisation(callback)).thenReturn(personalizationMap2);

        when(notificationSender.sendLetter(templateId1, address1, personalizationMap1, refId1, callback))
            .thenReturn(notificationId1);
        when(notificationSender.sendLetter(templateId2, address2, personalizationMap2, refId2, callback))
            .thenReturn(notificationId2);

        when(notificationIdAppender.append(notificationsSent, refId1, notificationId1)).thenReturn(notificationsSent);
        when(notificationIdAppender.append(notificationsSent, refId2, notificationId2)).thenReturn(notificationsSent);

        repEmailNotificationPersonalisationList =
            newArrayList(emailNotificationPersonalisation, emailNotificationPersonalisation1);
        aipEmailNotificationPersonalisationList =
            newArrayList(emailNotificationPersonalisation, emailNotificationPersonalisation1);
        aipSmsNotificationPersonalisationList =
            newArrayList(smsNotificationPersonalisation1, smsNotificationPersonalisation2);
        letterNotificationPersonalisationList =
            newArrayList(letterNotificationPersonalisation1, letterNotificationPersonalisation2);

        documentTagList = newArrayList(documentTag);

        when(notificationSender.sendPrecompiledLetter(refId3, mockInputStream))
            .thenReturn(notificationId1);
        when(notificationIdAppender.append(notificationsSent, refId3, notificationId1)).thenReturn(notificationsSent);

        when(asylumCase.read(LETTER_BUNDLE_DOCUMENTS)).thenReturn(Optional.of(newArrayList(documentWithMetadataId)));

        when(documentDownloadClient.download(documentBinaryUrl)).thenReturn(resource);
        when(resource.getInputStream()).thenReturn(mockInputStream);

    }

    @AfterEach
    public void cleanup() {
        mocked.close();
    }

    @Test
    public void should_send_notification_for_each_email_personalisation() {
        notificationGenerator =
            new EmailNotificationGenerator(repEmailNotificationPersonalisationList, notificationSender,
                notificationIdAppender);

        when(emailNotificationPersonalisation.getRecipientsList(asylumCase)).thenReturn(singleton(emailAddress1));
        when(emailNotificationPersonalisation1.getRecipientsList(asylumCase)).thenReturn(singleton(emailAddress2));

        notificationGenerator.generate(callback);

        verify(notificationSender).sendEmail(templateId1, emailAddress1, personalizationMap1, refId1, callback);
        verify(notificationSender).sendEmail(templateId2, emailAddress2, personalizationMap2, refId2, callback);

        verify(notificationIdAppender).appendAll(asylumCase, refId1, singletonList(notificationId1));
        verify(notificationIdAppender).append(notificationsSent, refId1, notificationId1);
        verify(notificationIdAppender).appendAll(asylumCase, refId2, singletonList(notificationId2));
        verify(notificationIdAppender).append(notificationsSent, refId2, notificationId2);

        verify(asylumCase, times(2)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_send_Aip_notification_emails_for_each_email_personalisation_using_the_subscriber_mode() {
        notificationGenerator =
            new EmailNotificationGenerator(aipEmailNotificationPersonalisationList, notificationSender,
                notificationIdAppender);

        when(emailNotificationPersonalisation.getRecipientsList(asylumCase)).thenReturn(singleton(emailAddress1));
        when(emailNotificationPersonalisation1.getRecipientsList(asylumCase)).thenReturn(singleton(emailAddress2));
        notificationGenerator.generate(callback);

        verify(notificationSender).sendEmail(templateId1, emailAddress1, personalizationMap1, refId1, callback);
        verify(notificationSender).sendEmail(templateId2, emailAddress2, personalizationMap2, refId2, callback);

        verify(notificationIdAppender).appendAll(asylumCase, refId1, singletonList(notificationId1));
        verify(notificationIdAppender).append(notificationsSent, refId1, notificationId1);
        verify(notificationIdAppender).appendAll(asylumCase, refId2, singletonList(notificationId2));
        verify(notificationIdAppender).append(notificationsSent, refId2, notificationId2);

        verify(asylumCase, times(2)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_send_Aip_notification_Sms_for_each_personalisation_using_the_subscriber_mode() {

        notificationGenerator = new SmsNotificationGenerator(aipSmsNotificationPersonalisationList, notificationSender,
            notificationIdAppender);

        when(smsNotificationPersonalisation1.getRecipientsList(asylumCase)).thenReturn(singleton(phoneNumber1));
        when(smsNotificationPersonalisation2.getRecipientsList(asylumCase)).thenReturn(singleton(phoneNumber2));

        notificationGenerator.generate(callback);

        verify(notificationSender).sendSms(templateId1, phoneNumber1, personalizationMap1, refId1, callback);
        verify(notificationSender).sendSms(templateId2, phoneNumber2, personalizationMap2, refId2, callback);

        verify(notificationIdAppender).appendAll(asylumCase, refId1, singletonList(notificationId1));
        verify(notificationIdAppender).append(notificationsSent, refId1, notificationId1);
        verify(notificationIdAppender).appendAll(asylumCase, refId2, singletonList(notificationId2));
        verify(notificationIdAppender).append(notificationsSent, refId2, notificationId2);

        verify(asylumCase, times(2)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_send_notification_for_each_letter_personalisation() {
        notificationGenerator =
            new LetterNotificationGenerator(letterNotificationPersonalisationList, notificationSender,
                notificationIdAppender);

        when(letterNotificationPersonalisation1.getRecipientsList(asylumCase)).thenReturn(singleton(address1));
        when(letterNotificationPersonalisation2.getRecipientsList(asylumCase)).thenReturn(singleton(address2));

        notificationGenerator.generate(callback);

        verify(notificationSender).sendLetter(templateId1, address1, personalizationMap1, refId1, callback);
        verify(notificationSender).sendLetter(templateId2, address2, personalizationMap2, refId2, callback);

        verify(notificationIdAppender).appendAll(asylumCase, refId1, singletonList(notificationId1));
        verify(notificationIdAppender).append(notificationsSent, refId1, notificationId1);
        verify(notificationIdAppender).appendAll(asylumCase, refId2, singletonList(notificationId2));
        verify(notificationIdAppender).append(notificationsSent, refId2, notificationId2);

        verify(asylumCase, times(2)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_send_notification_for_each_precompiled_letter_document_tag() {

        notificationGenerator =
            new PrecompiledLetterNotificationGenerator(documentTagList, notificationSender,
                notificationIdAppender, documentDownloadClient);

        notificationGenerator.generate(callback);

        verify(notificationSender).sendPrecompiledLetter(refId3, mockInputStream);
        verify(notificationIdAppender).append(notificationsSent, refId3, notificationId1);
        verify(notificationIdAppender).appendAll(asylumCase, refId3, singletonList(notificationId1));
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


    @Test
    public void should_not_send_notification_when_invalid_email_address() {
        notificationGenerator =
                new EmailNotificationGenerator(aipEmailNotificationPersonalisationList, notificationSender,
                        notificationIdAppender);

        when(emailNotificationPersonalisation.getRecipientsList(asylumCase)).thenReturn(singleton(NO_EMAIL_ADDRESS_DECISION_WITHOUT_HEARING));
        when(emailNotificationPersonalisation1.getRecipientsList(asylumCase)).thenReturn(singleton(emailAddress2));

        notificationGenerator.generate(callback);

        verify(notificationSender).sendEmail(templateId2, emailAddress2, personalizationMap2, refId2, callback);
        verifyNoMoreInteractions(notificationSender);

        verify(notificationIdAppender).appendAll(asylumCase, refId1, emptyList());
        verify(notificationIdAppender).appendAll(asylumCase, refId2, singletonList(notificationId2));
        verify(notificationIdAppender).append(notificationsSent, refId2, notificationId2);
        verifyNoMoreInteractions(notificationIdAppender);

        verify(asylumCase, times(1)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_not_send_notification_when_sms_personalisation_list_empty() {
        notificationGenerator = new SmsNotificationGenerator(emptyList(), notificationSender, notificationIdAppender);
        notificationGenerator.generate(callback);

        verifyNoInteractions(notificationSender);
        verifyNoInteractions(notificationIdAppender);

        verify(asylumCase, times(0)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_not_send_notification_when_letter_personalisation_list_empty() {
        notificationGenerator = new LetterNotificationGenerator(emptyList(), notificationSender, notificationIdAppender);
        notificationGenerator.generate(callback);

        verifyNoInteractions(notificationSender);
        verifyNoInteractions(notificationIdAppender);

        verify(asylumCase, times(0)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_not_send_notification_when_precompiled_letter_document_tag_list_empty() {
        notificationGenerator = new PrecompiledLetterNotificationGenerator(emptyList(), notificationSender, notificationIdAppender, documentDownloadClient);
        notificationGenerator.generate(callback);

        verifyNoInteractions(notificationSender);
        verifyNoInteractions(notificationIdAppender);

        verify(asylumCase, times(0)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }
}
