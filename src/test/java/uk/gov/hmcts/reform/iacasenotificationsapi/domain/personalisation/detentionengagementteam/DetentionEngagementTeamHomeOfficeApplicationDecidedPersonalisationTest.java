package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.TestUtils.getDocumentWithMetadata;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetentionEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionEngagementTeamHomeOfficeApplicationDecidedPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    DetentionEmailService detentionEmailService;
    @Mock
    DocumentDownloadClient documentDownloadClient;

    private final Long caseId = 12345L;
    private final String templateId = "someTemplateId";
    private final String nonAdaPrefix = "IAFT - SERVE IN PERSON";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";

    DocumentWithMetadata appealCanProceedLetter = getDocumentWithMetadata(
            "1",
            "home-office-application-decided-letter",
            "Home office application decided letter",
            DocumentTag.HOME_OFFICE_APPLICATION_DECIDED_LETTER);

    IdValue<DocumentWithMetadata> appealCanProceedLetterId = new IdValue<>("1", appealCanProceedLetter);
    private final JSONObject jsonObject = new JSONObject("{\"title\": \"JsonDocument\"}");

    private DetentionEngagementTeamHomeOfficeApplicationDecidedPersonalisation detentionEngagementTeamHomeOfficeApplicationDecidedPersonalisation;

    DetentionEngagementTeamHomeOfficeApplicationDecidedPersonalisationTest() {
    }

    @BeforeEach
    void setup() throws NotificationClientException, IOException {
        detentionEngagementTeamHomeOfficeApplicationDecidedPersonalisation = new DetentionEngagementTeamHomeOfficeApplicationDecidedPersonalisation(
                templateId,
                nonAdaPrefix,
                detentionEmailService,
                documentDownloadClient
        );

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(appealCanProceedLetterId)));
        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(jsonObject);
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_HOME_OFFICE_APPLICATION_DECIDED",
            detentionEngagementTeamHomeOfficeApplicationDecidedPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, detentionEngagementTeamHomeOfficeApplicationDecidedPersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_det_email_address() {
        String detentionEngagementTeamEmail = "det@email.com";
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detentionEmailService.getDetentionEmailAddress(asylumCase)).thenReturn(detentionEngagementTeamEmail);

        assertTrue(
            detentionEngagementTeamHomeOfficeApplicationDecidedPersonalisation.getRecipientsList(asylumCase).contains(detentionEngagementTeamEmail));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_no_detention_facility() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());
        assertEquals(Collections.emptySet(), detentionEngagementTeamHomeOfficeApplicationDecidedPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_empty_set_when_appellant_not_in_detention() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertEquals(Collections.emptySet(), detentionEngagementTeamHomeOfficeApplicationDecidedPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_empty_set_when_detention_facility_is_other() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        assertEquals(Collections.emptySet(), detentionEngagementTeamHomeOfficeApplicationDecidedPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    void should_return_personalisation_of_all_information() throws NotificationClientException, IOException {
        Map<String, Object> personalisation = detentionEngagementTeamHomeOfficeApplicationDecidedPersonalisation.getPersonalisationForLink(asylumCase);

        assertEquals(nonAdaPrefix, personalisation.get("subjectPrefix"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(jsonObject, personalisation.get("documentLink"));
    }

    @Test
    void should_return_personalisation_with_empty_strings_when_case_fields_are_empty() throws NotificationClientException, IOException {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, Object> personalisation = detentionEngagementTeamHomeOfficeApplicationDecidedPersonalisation.getPersonalisationForLink(asylumCase);

        assertEquals(nonAdaPrefix, personalisation.get("subjectPrefix"));
        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals(jsonObject, personalisation.get("documentLink"));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(() -> detentionEngagementTeamHomeOfficeApplicationDecidedPersonalisation.getPersonalisationForLink((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_throw_exception_when_appeal_can_proceed_document_is_empty() {
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> detentionEngagementTeamHomeOfficeApplicationDecidedPersonalisation.getPersonalisationForLink(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("homeOfficeApplicationDecidedLetter document not available");
    }

    @Test
    public void should_throw_exception_when_notification_client_throws_Exception() throws NotificationClientException, IOException {
        when(documentDownloadClient.getJsonObjectFromDocument(appealCanProceedLetter)).thenThrow(new NotificationClientException("File size is more than 2MB"));
        assertThatThrownBy(() -> detentionEngagementTeamHomeOfficeApplicationDecidedPersonalisation.getPersonalisationForLink(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to get Internal 'Appeal can proceed' Letter in compatible format");
    }

    @Test
    public void should_throw_exception_when_io_exception_occurs() throws NotificationClientException, IOException {
        when(documentDownloadClient.getJsonObjectFromDocument(appealCanProceedLetter)).thenThrow(new IOException("IO Exception occurred"));
        assertThatThrownBy(() -> detentionEngagementTeamHomeOfficeApplicationDecidedPersonalisation.getPersonalisationForLink(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to get Internal 'Appeal can proceed' Letter in compatible format");
    }
}