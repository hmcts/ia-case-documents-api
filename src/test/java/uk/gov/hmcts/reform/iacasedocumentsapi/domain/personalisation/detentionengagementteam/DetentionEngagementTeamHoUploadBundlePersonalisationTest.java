package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DetentionEmailService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadClient;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.TestUtils.getDocumentWithMetadata;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionEngagementTeamHoUploadBundlePersonalisationTest {

    private static final String TEMPLATE_ID = "template123";
    private static final String NON_ADA_PREFIX = "[Non-ADA]";
    private static final String CTSC_EMAIL = "ctsc@example.com";
    private static final String DETENTION_EMAIL = "detention@example.com";

    @Mock
    private DetentionEmailService detentionEmailService;

    @Mock
    private DocumentDownloadClient documentDownloadClient;

    @Mock
    private AsylumCase asylumCase;

    private DetentionEngagementTeamHoUploadBundlePersonalisation personalisation;

    @BeforeEach
    void setUp() {
        personalisation = new DetentionEngagementTeamHoUploadBundlePersonalisation(
                TEMPLATE_ID,
                NON_ADA_PREFIX,
                detentionEmailService,
                CTSC_EMAIL,
                documentDownloadClient
        );
    }

    @Test
    void shouldReturnReferenceId() {
        String refId = personalisation.getReferenceId(123L);

        assertThat(refId).isEqualTo("123_INTERNAL_DETAINED_APPEAL_HO_UPLOAD_BUNDLE_APPELLANT_LETTER");
    }

    @Test
    void shouldReturnDetentionEmailAddress_whenDetainedInIrc() {
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("A123"));
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("HO123"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("John"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Doe"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detentionEmailService.getDetentionEmailAddress(asylumCase)).thenReturn(DETENTION_EMAIL);

        Set<String> recipients = personalisation.getRecipientsList(asylumCase);

        assertThat(recipients).containsExactly(DETENTION_EMAIL);
    }

    @Test
    void shouldReturnCtscEmailAddress_whenNotInIrc() {
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("A123"));
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("HO123"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("John"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Doe"));

        Set<String> recipients = personalisation.getRecipientsList(asylumCase);

        assertThat(recipients).containsExactly(CTSC_EMAIL);
    }

    @Test
    void shouldReturnTemplateId() {
        assertThat(personalisation.getTemplateId()).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnPersonalisationMap() throws Exception {
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("A123"));
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("HO123"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("John"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Doe"));

        DocumentWithMetadata internalFtpaDecidedByRjLetter = getDocumentWithMetadata(
                "1", "Letter", "desc", DocumentTag.INTERNAL_DETAINED_APPEAL_HO_UPLOAD_BUNDLE_APPELLANT_LETTER);
        IdValue<DocumentWithMetadata> doc = new IdValue<>("1", internalFtpaDecidedByRjLetter);
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(doc)));
        JSONObject dummyJson = new JSONObject().put("link", "http://doc");
        when(documentDownloadClient.getJsonObjectFromDocument(any())).thenReturn(dummyJson);

        Map<String, Object> map = personalisation.getPersonalisationForLink(asylumCase);

        assertThat(map)
                .containsEntry("subjectPrefix", NON_ADA_PREFIX)
                .containsEntry("appealReferenceNumber", "A123")
                .containsEntry("homeOfficeReferenceNumber", "HO123")
                .containsEntry("appellantGivenNames", "John")
                .containsEntry("appellantFamilyName", "Doe")
                .containsEntry("documentLink", dummyJson);
    }

    @Test
    void shouldThrowException_whenDocumentDownloadFails() throws Exception {
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("A123"));
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("HO123"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("John"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Doe"));

        DocumentWithMetadata internalFtpaDecidedByRjLetter = getDocumentWithMetadata(
                "1", "Letter", "desc", DocumentTag.INTERNAL_DETAINED_APPEAL_HO_UPLOAD_BUNDLE_APPELLANT_LETTER);
        IdValue<DocumentWithMetadata> doc = new IdValue<>("1", internalFtpaDecidedByRjLetter);
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(doc)));

        when(documentDownloadClient.getJsonObjectFromDocument(any())).thenThrow(new IOException("fail"));

        assertThatThrownBy(() -> personalisation.getPersonalisationForLink(asylumCase))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to get Internal 'Home Office to upload bundle' Letter");
    }
}
