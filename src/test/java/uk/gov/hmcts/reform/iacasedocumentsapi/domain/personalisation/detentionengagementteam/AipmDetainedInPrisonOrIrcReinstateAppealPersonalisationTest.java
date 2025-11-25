package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.TestUtils.compareStringsAndJsonObjects;
import static uk.gov.hmcts.reform.iacasedocumentsapi.TestUtils.getDocumentWithMetadata;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DetentionEmailService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AipmDetainedInPrisonOrIrcReinstateAppealPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    DetentionEmailService detentionEmailService;
    @Mock
    JSONObject jsonDocument;
    @Mock
    DocumentDownloadClient documentDownloadClient;

    private String detentionEngagementTeamTemplateId = "detentionEngagementTeamTemplateId";
    private final String uploadAdditionalEvidencePersonalisationReferenceId = "_AIPM_DETAINED_IN_PRISON_IRC_REINSTATE_APPEAL_LETTER";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String detEmailAddress = "some@example.com";
    private final String nonAdaPrefix = "IAFT - SERVE IN PERSON";
    private final Long caseId = 12345L;

    private AipmDetainedInPrisonOrIrcReinstateAppealPersonalisation aipmDetainedInPrisonOrIrcReinstateAppealPersonalisation;
    DocumentWithMetadata reinstateAppealDoc = getDocumentWithMetadata(
        "1", "aipm-detained-in-prison-irc-reinstate-appeal-letter", "some other desc", DocumentTag.AIPM_DETAINED_IN_PRISON_IRC_REINSTATE_APPEAL_LETTER);
    IdValue<DocumentWithMetadata> reinstateAppealDocId = new IdValue<>("1", reinstateAppealDoc);

    AipmDetainedInPrisonOrIrcReinstateAppealPersonalisationTest() {
    }

    @BeforeEach
    void setup() throws NotificationClientException, IOException {
        aipmDetainedInPrisonOrIrcReinstateAppealPersonalisation = new AipmDetainedInPrisonOrIrcReinstateAppealPersonalisation(
            detentionEngagementTeamTemplateId,
            nonAdaPrefix,
            detentionEmailService,
            documentDownloadClient
        );
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(reinstateAppealDocId)));

        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(jsonDocument);
    }

    @Test
    public void should_return_template_id() {
        assertEquals(
            detentionEngagementTeamTemplateId,
            aipmDetainedInPrisonOrIrcReinstateAppealPersonalisation.getTemplateId()
        );
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + uploadAdditionalEvidencePersonalisationReferenceId,
            aipmDetainedInPrisonOrIrcReinstateAppealPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case_when_detained_in_irc() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detentionEmailService.getDetentionEmailAddress(asylumCase)).thenReturn(detEmailAddress);

        assertTrue(
            aipmDetainedInPrisonOrIrcReinstateAppealPersonalisation.getRecipientsList(asylumCase).contains(detEmailAddress));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case_when_detained_in_prison() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));
        when(detentionEmailService.getDetentionEmailAddress(asylumCase)).thenReturn(detEmailAddress);

        assertTrue(
            aipmDetainedInPrisonOrIrcReinstateAppealPersonalisation.getRecipientsList(asylumCase).contains(detEmailAddress));
    }

    @Test
    public void should_return_empty_set_when_appellant_not_in_detention() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertEquals(Collections.emptySet(), aipmDetainedInPrisonOrIrcReinstateAppealPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_no_detention_facility() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());
        assertEquals(Collections.emptySet(), aipmDetainedInPrisonOrIrcReinstateAppealPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_other_detention_facility() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        assertEquals(Collections.emptySet(), aipmDetainedInPrisonOrIrcReinstateAppealPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(
            () -> aipmDetainedInPrisonOrIrcReinstateAppealPersonalisation.getPersonalisationForLink((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_document_is_missing() {
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> aipmDetainedInPrisonOrIrcReinstateAppealPersonalisation.getPersonalisationForLink(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("aipmDetainedInPrisonOrIrcReinstateAppealLetter document not available");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() throws IOException, NotificationClientException {
        final Map<String, Object> expectedPersonalisation =
            ImmutableMap
                .<String, Object>builder()
                .put("subjectPrefix", nonAdaPrefix)
                .put("appealReferenceNumber", appealReferenceNumber)
                .put("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantFamilyName)
                .put("documentLink", jsonDocument)
                .build();

        Map<String, Object> actualPersonalisation =
            aipmDetainedInPrisonOrIrcReinstateAppealPersonalisation.getPersonalisationForLink(asylumCase);

        assertTrue(compareStringsAndJsonObjects(expectedPersonalisation, actualPersonalisation));
    }

    @Test
    public void should_throw_exception_when_notification_client_throws_Exception() throws NotificationClientException, IOException {
        when(documentDownloadClient.getJsonObjectFromDocument(reinstateAppealDoc)).thenThrow(new NotificationClientException("File size is more than 2MB"));
        assertThatThrownBy(() -> aipmDetainedInPrisonOrIrcReinstateAppealPersonalisation.getPersonalisationForLink(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Failed to get Internal 'Appeal can proceed' Letter in compatible format");
    }
}
