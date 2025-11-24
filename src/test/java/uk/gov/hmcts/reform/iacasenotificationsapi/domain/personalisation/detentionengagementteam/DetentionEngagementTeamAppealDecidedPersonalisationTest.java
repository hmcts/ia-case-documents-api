package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

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
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.TestUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealDecision;
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
class DetentionEngagementTeamAppealDecidedPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    private DocumentDownloadClient documentDownloadClient;
    @Mock
    private DetentionEmailService detEmailService;
    private final JSONObject jsonObject = new JSONObject("{\"title\": \"Test JsonDocument\"}");
    private final AppealDecision appealDismissed = AppealDecision.DISMISSED;
    private final AppealDecision appealAllowed = AppealDecision.ALLOWED;

    private final Long caseId = 12345L;
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String detentionEngagementTeamUploadAppealDecidedDismissedTemplateId = "detentionEngagementTeamUploadAppealDecidedDismissedTemplateId";
    private final String detentionEngagementTeamUploadAppealDecidedAllowedTemplateId = "detentionEngagementTeamUploadAppealDecidedAllowedTemplateId";
    private final String formNameForAdaDismissed = "IAFT-ADA5: Ask for permission to appeal to the Upper Tribunal (Immigration and Asylum Chamber) – Accelerated detained appeal (ADA)";
    private final String formLinkForAdaDismissed = "This form can be found here: http://www.gov.uk/government/publications/ask-for-permission-to-appeal-to-the-upper-tribunal-immigration-and-asylum-chamber-accelerated-detained-appeal-form-iaftada5";
    private final String formNameForNonAdaDismissed = "IAFT-DE5: Ask for permission to appeal to the Upper Tribunal (Immigration and Asylum Chamber) – Detained appeal";
    private final String formLinkForNonAdaDismissed = "This form can be found here: https://www.gov.uk/government/publications/ask-for-permission-to-appeal-to-the-upper-tribunal-immigration-and-asylum-chamber-detained-appeal-form-iaft-de5";
    DocumentWithMetadata appealDecidedDoc = TestUtils.getDocumentWithMetadata(
            "id", "appeal_decided", "some other desc", DocumentTag.INTERNAL_DET_DECISION_AND_REASONS_LETTER);
    IdValue<DocumentWithMetadata> appealDecided = new IdValue<>("1", appealDecidedDoc);
    private DetentionEngagementTeamAppealDecidedPersonalisation detentionEngagementTeamAppealDecidedPersonalisation;

    public DetentionEngagementTeamAppealDecidedPersonalisationTest() {
    }

    @BeforeEach
    void setup() throws NotificationClientException, IOException {
        detentionEngagementTeamAppealDecidedPersonalisation = new DetentionEngagementTeamAppealDecidedPersonalisation(
                detentionEngagementTeamUploadAppealDecidedDismissedTemplateId,
                detentionEngagementTeamUploadAppealDecidedAllowedTemplateId,
                detEmailService,
                documentDownloadClient
        );

        ReflectionTestUtils.setField(detentionEngagementTeamAppealDecidedPersonalisation, "adaSubjectPrefix", "ADA - SERVE IN PERSON");
        ReflectionTestUtils.setField(detentionEngagementTeamAppealDecidedPersonalisation, "iaftSubjectPrefix", "IAFT - SERVE IN PERSON");
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HEARING_DOCUMENTS)).thenReturn(Optional.of(newArrayList(appealDecided)));
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(appealDecided)));
        when(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)).thenReturn(Optional.of(appealDismissed));
        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(jsonObject);
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_INTERNAL_DET_APPEAL_DECIDED_EMAIL",
                detentionEngagementTeamAppealDecidedPersonalisation.getReferenceId(caseId));
    }


    @Test
    void should_return_given_det_email_address() {
        String detentionEngagementTeamEmail = "det@email.com";
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detEmailService.getDetentionEmailAddress(asylumCase)).thenReturn(detentionEngagementTeamEmail);

        assertTrue(
                detentionEngagementTeamAppealDecidedPersonalisation.getRecipientsList(asylumCase).contains(detentionEngagementTeamEmail));
    }

    @Test
    void should_return_empty_set_email_address_from_asylum_case_no_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());
        assertEquals(Collections.emptySet(), detentionEngagementTeamAppealDecidedPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    void should_return_empty_set_email_address_from_asylum_case_other_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        assertEquals(Collections.emptySet(), detentionEngagementTeamAppealDecidedPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> detentionEngagementTeamAppealDecidedPersonalisation.getPersonalisationForLink((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_if_decision_dismissed_for_ada() throws NotificationClientException, IOException {
        Map<String, Object> personalisation = detentionEngagementTeamAppealDecidedPersonalisation.getPersonalisationForLink(asylumCase);

        assertEquals("ADA - SERVE IN PERSON", personalisation.get("subjectPrefix"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(jsonObject, personalisation.get("documentLink"));
        assertEquals(formNameForAdaDismissed, personalisation.get("formName"));
        assertEquals(formLinkForAdaDismissed, personalisation.get("formLinkText"));
    }

    @Test
    void should_return_personalisation_if_decision_dismissed_for_nonAda() throws NotificationClientException, IOException {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(NO));
        Map<String, Object> personalisation = detentionEngagementTeamAppealDecidedPersonalisation.getPersonalisationForLink(asylumCase);

        assertEquals("IAFT - SERVE IN PERSON", personalisation.get("subjectPrefix"));
        assertEquals(formNameForNonAdaDismissed, personalisation.get("formName"));
        assertEquals(formLinkForNonAdaDismissed, personalisation.get("formLinkText"));
    }

    @Test
    void should_not_add_form_name_and_link_if_decision_allowed() throws NotificationClientException, IOException {
        when(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)).thenReturn(Optional.of(appealAllowed));
        Map<String, Object> personalisation = detentionEngagementTeamAppealDecidedPersonalisation.getPersonalisationForLink(asylumCase);

        assertNull(personalisation.get("formName"));
        assertNull(personalisation.get("formLinkText"));
    }

}