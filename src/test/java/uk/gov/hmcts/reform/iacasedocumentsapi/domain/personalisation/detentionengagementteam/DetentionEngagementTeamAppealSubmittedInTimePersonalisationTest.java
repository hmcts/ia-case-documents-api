package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam.DetentionEngagementTeamAppealSubmittedInTimePersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.TestUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionEngagementTeamAppealSubmittedInTimePersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    private DocumentDownloadClient documentDownloadClient;
    @Mock
    private DetEmailService detEmailService;

    private final Long caseId = 12345L;
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String nonAdaPrefix = "IAFT - SERVE IN PERSON";
    private final String appealSubmittedNonAdaInTimeDetainedOnlyTemplateId = "test template";
    private final JSONObject jsonObject = new JSONObject("{\"title\": \"Hearing bundle JsonDocument\"}");
    DocumentWithMetadata internalAppealSubmissionDoc = TestUtils.getDocumentWithMetadata(
            "id", "internal_appeal_submission", "some other desc", DocumentTag.INTERNAL_APPEAL_SUBMISSION);
    IdValue<DocumentWithMetadata> appealSubmittedBundle = new IdValue<>("1", internalAppealSubmissionDoc);
    private DetentionEngagementTeamAppealSubmittedInTimePersonalisation detentionEngagementTeamAppealSubmittedInTimePersonalisation;

    DetentionEngagementTeamAppealSubmittedInTimePersonalisationTest() {
    }

    @BeforeEach
    void setup() throws NotificationClientException, IOException {
        detentionEngagementTeamAppealSubmittedInTimePersonalisation = new DetentionEngagementTeamAppealSubmittedInTimePersonalisation(
                appealSubmittedNonAdaInTimeDetainedOnlyTemplateId,
                nonAdaPrefix,
                detEmailService,
                documentDownloadClient
        );
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(appealSubmittedBundle)));
        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(jsonObject);
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_INTERNAL_NON_ADA_APPEAL_SUBMITTED",
                detentionEngagementTeamAppealSubmittedInTimePersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_det_email_address() {
        String detentionEngagementTeamEmail = "det@email.com";
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detEmailService.getRecipientsList(asylumCase)).thenReturn(Collections.singleton(detentionEngagementTeamEmail));

        assertTrue(
                detentionEngagementTeamAppealSubmittedInTimePersonalisation.getRecipientsList(asylumCase).contains(detentionEngagementTeamEmail));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_no_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());
        assertEquals(Collections.emptySet(), detentionEngagementTeamAppealSubmittedInTimePersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_other_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        assertEquals(Collections.emptySet(), detentionEngagementTeamAppealSubmittedInTimePersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    void should_return_personalisation_of_all_information() throws NotificationClientException, IOException {
        Map<String, Object> personalisation = detentionEngagementTeamAppealSubmittedInTimePersonalisation.getPersonalisationForLink(asylumCase);

        assertEquals(nonAdaPrefix, personalisation.get("subjectPrefix"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(jsonObject, personalisation.get("documentLink"));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> detentionEngagementTeamAppealSubmittedInTimePersonalisation.getPersonalisationForLink((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_throw_exception_when_appeal_submission_is_empty() {
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> detentionEngagementTeamAppealSubmittedInTimePersonalisation.getPersonalisationForLink(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("internalAppealSubmission document not available");
    }

    @Test
    public void should_throw_exception_when_notification_client_throws_Exception() throws NotificationClientException, IOException {
        when(documentDownloadClient.getJsonObjectFromDocument(internalAppealSubmissionDoc)).thenThrow(new NotificationClientException("File size is more than 2MB"));
        assertThatThrownBy(() -> detentionEngagementTeamAppealSubmittedInTimePersonalisation.getPersonalisationForLink(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to get Internal Appeal submission Letter in compatible format");
    }

}
