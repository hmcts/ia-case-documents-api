package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.TestUtils.compareStringsAndJsonObjects;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam.DetentionEngagementTeamUpdateHearingAdjustmentsPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.TestUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionEngagementTeamUpdateHearingAdjustmentsPersonalisationTest {
    @Mock
    AsylumCase asylumCase;
    @Mock
    private DocumentDownloadClient documentDownloadClient;
    @Mock
    private DetEmailService detEmailService;
    @Mock
    JSONObject jsonDocument;
    private String templateId = "templateId";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String nonAdaPrefix = "IAFT - SERVE IN PERSON";
    private final String adaPrefix = "ADA - SERVE IN PERSON";
    private final Long caseId = 12345L;
    private DetentionEngagementTeamUpdateHearingAdjustmentsPersonalisation detentionEngagementTeamUpdateHearingAdjustmentsPersonalisation;

    DocumentWithMetadata hearingAdjustmentsChangedDoc = TestUtils.getDocumentWithMetadata(
        "id", "hearing-adjustments-updated-letter", "some other desc", DocumentTag.INTERNAL_HEARING_ADJUSTMENTS_UPDATED_LETTER);
    IdValue<DocumentWithMetadata> hearingAdjustmentsChangedBundle = new IdValue<>("1", hearingAdjustmentsChangedDoc);

    DetentionEngagementTeamUpdateHearingAdjustmentsPersonalisationTest() {
    }

    @BeforeEach
    public void setUp() throws NotificationClientException, IOException {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(hearingAdjustmentsChangedBundle)));
        when(documentDownloadClient.getJsonObjectFromDocument(hearingAdjustmentsChangedDoc)).thenReturn(jsonDocument);

        detentionEngagementTeamUpdateHearingAdjustmentsPersonalisation =
            new DetentionEngagementTeamUpdateHearingAdjustmentsPersonalisation(
                templateId,
                detEmailService,
                documentDownloadClient,
                adaPrefix,
                nonAdaPrefix
            );
    }

    @Test
    public void should_return_given_template_id_detained() {
        assertEquals(
            templateId,
            detentionEngagementTeamUpdateHearingAdjustmentsPersonalisation.getTemplateId()
        );
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_INTERNAL_HEARING_ADJUSTMENTS_CHANGED_DET",
            detentionEngagementTeamUpdateHearingAdjustmentsPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_det_email_address() {
        String detentionEngagementTeamEmail = "det@email.com";
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detEmailService.getRecipientsList(asylumCase)).thenReturn(Collections.singleton(detentionEngagementTeamEmail));

        assertTrue(
            detentionEngagementTeamUpdateHearingAdjustmentsPersonalisation.getRecipientsList(asylumCase).contains(detentionEngagementTeamEmail));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_no_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());
        assertEquals(Collections.emptySet(), detentionEngagementTeamUpdateHearingAdjustmentsPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_other_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        assertEquals(Collections.emptySet(), detentionEngagementTeamUpdateHearingAdjustmentsPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_personalisation_when_all_information_given_refused() {

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
            detentionEngagementTeamUpdateHearingAdjustmentsPersonalisation.getPersonalisationForLink(asylumCase);

        assertTrue(compareStringsAndJsonObjects(expectedPersonalisation, actualPersonalisation));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> detentionEngagementTeamUpdateHearingAdjustmentsPersonalisation.getPersonalisationForLink((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_throw_exception_when_appeal_submission_is_empty() {
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> detentionEngagementTeamUpdateHearingAdjustmentsPersonalisation.getPersonalisationForLink(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("internalHearingAdjustmentsUpdatedLetter document not available");
    }

    @Test
    public void should_throw_exception_when_notification_client_throws_Exception() throws NotificationClientException, IOException {
        when(documentDownloadClient.getJsonObjectFromDocument(hearingAdjustmentsChangedDoc)).thenThrow(new NotificationClientException("File size is more than 2MB"));
        assertThatThrownBy(() -> detentionEngagementTeamUpdateHearingAdjustmentsPersonalisation.getPersonalisationForLink(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Failed to get Internal Hearing Adjustments changed document in compatible format");
    }
}
