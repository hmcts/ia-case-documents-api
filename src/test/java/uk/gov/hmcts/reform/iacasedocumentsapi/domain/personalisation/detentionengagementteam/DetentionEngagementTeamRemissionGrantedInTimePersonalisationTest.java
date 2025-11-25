package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DetentionEmailService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadClient;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag.INTERNAL_DETAINED_APPEAL_REMISSION_GRANTED_IN_TIME_LETTER;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionEngagementTeamRemissionGrantedInTimePersonalisationTest {

    private static final String TEMPLATE_ID = "template123";
    private static final String NON_ADA_PREFIX = "[Non-ADA]";
    private static final String CTSC_EMAIL = "ctsc@example.com";
    private static final String DETENTION_EMAIL = "detention@example.com";

    @Mock
    private DetEmailService detEmailService;

    @Mock
    private DocumentDownloadClient documentDownloadClient;

    private DetentionEmailService detentionEmailService;
    private DetentionEngagementTeamRemissionGrantedInTimePersonalisation personalisation;

    @BeforeEach
    void setUp() {
        detentionEmailService = new DetentionEmailService(detEmailService, CTSC_EMAIL);
        personalisation = new DetentionEngagementTeamRemissionGrantedInTimePersonalisation(
                TEMPLATE_ID,
                NON_ADA_PREFIX,
                detentionEmailService,
                documentDownloadClient
        );
    }

    @Test
    void shouldReturnReferenceId() {
        String refId = personalisation.getReferenceId(123L);
        assertThat(refId).isEqualTo("123_INTERNAL_DETAINED_APPEAL_REMISSION_GRANTED_IN_TIME;");
    }

    @Test
    void shouldReturnDetentionEmailAddress_whenDetainedInIrc() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detEmailService.getDetEmailAddress(asylumCase)).thenReturn(DETENTION_EMAIL);

        try (var mocked = Mockito.mockStatic(
                uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.class)) {
            mocked.when(() -> uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils
                            .isDetainedInFacilityType(asylumCase, DetentionFacility.IRC))
                    .thenReturn(true);

            Set<String> recipients = personalisation.getRecipientsList(asylumCase);

            assertThat(recipients).containsExactly(DETENTION_EMAIL);
        }
    }

    @Test
    void shouldReturnCtscEmailAddress_whenNotInIrc() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));

        try (var mocked = Mockito.mockStatic(
                uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.class)) {
            mocked.when(() -> uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils
                            .isDetainedInFacilityType(asylumCase, DetentionFacility.IRC))
                    .thenReturn(false);

            Set<String> recipients = personalisation.getRecipientsList(asylumCase);

            assertThat(recipients).containsExactly(CTSC_EMAIL);
        }
    }

    @Test
    void shouldReturnTemplateId() {
        assertThat(personalisation.getTemplateId()).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnPersonalisationMap() throws Exception {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("A123"));
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("HO123"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("John"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Doe"));

        JSONObject dummyJson = new JSONObject().put("link", "http://doc");
        try (var mocked = Mockito.mockStatic(
                uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.class)) {
            mocked.when(() -> uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils
                            .getLetterForNotification(asylumCase, INTERNAL_DETAINED_APPEAL_REMISSION_GRANTED_IN_TIME_LETTER))
                    .thenReturn(null);

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
    }

    @Test
    void shouldThrowException_whenDocumentDownloadFails() throws Exception {
        AsylumCase asylumCase = mock(AsylumCase.class);
        try (var mocked = Mockito.mockStatic(
                uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.class)) {
            mocked.when(() -> uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils
                            .getLetterForNotification(asylumCase, INTERNAL_DETAINED_APPEAL_REMISSION_GRANTED_IN_TIME_LETTER))
                    .thenReturn(null);

            when(documentDownloadClient.getJsonObjectFromDocument(any())).thenThrow(new IOException("fail"));

            assertThatThrownBy(() -> personalisation.getPersonalisationForLink(asylumCase))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Failed to get Internal 'Remission granted' Letter");
        }
    }
}
