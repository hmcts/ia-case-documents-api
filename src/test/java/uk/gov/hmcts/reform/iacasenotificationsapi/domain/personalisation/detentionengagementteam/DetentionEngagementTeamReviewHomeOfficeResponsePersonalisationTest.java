package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.TestUtils.compareStringsAndJsonObjects;
import static uk.gov.hmcts.reform.iacasenotificationsapi.TestUtils.getDocumentWithMetadata;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealReviewOutcome;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DetentionEngagementTeamReviewHomeOfficeResponsePersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    DetEmailService detEmailService;
    @Mock
    JSONObject jsonDocument;
    @Mock
    DocumentDownloadClient documentDownloadClient;
    @Mock
    private PersonalisationProvider personalisationProvider;

    private final String templateId = "someTemplateId";
    private final String detentionEngagementTeamReviewHomeOfficeResponseersonalisationReferenceId = "_INTERNAL_DETAINED_REVIEW_HOME_OFFICE_RESPONSE_DET";
    private final String detEmailAddress = "some@example.com";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "someReferenceNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String subjectPrefix = "IAFT - SERVE BY POST";
    private final String homeOfficeAppealReviewMaintainedDocumentName = "Home Office Response";
    private final String homeOfficeAppealReviewWithdrawnDocumentName = "Withdrawal Letter";
    DocumentWithMetadata internalDetainedReviewHomeOfficeResponseDecisionMaintainedLetter = getDocumentWithMetadata(
            "1", "Detained appellant letter_HO response when decision maintained", "some other desc", DocumentTag.INTERNAL_DETAINED_REQUEST_HO_RESPONSE_REVIEW);
    IdValue<DocumentWithMetadata> internalDetainedReviewHomeOfficeResponseDecisionMaintainedLetterId = new IdValue<>("1", internalDetainedReviewHomeOfficeResponseDecisionMaintainedLetter);

    DocumentWithMetadata internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetter = getDocumentWithMetadata(
            "1", "Detained appellant letter_HO response when decision withdrawn", "some other desc", DocumentTag.INTERNAL_DETAINED_REQUEST_HO_RESPONSE_REVIEW);
    IdValue<DocumentWithMetadata> internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterId = new IdValue<>("1", internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetter);
    private DetentionEngagementTeamReviewHomeOfficeResponsePersonalisation detentionEngagementTeamReviewHomeOfficeResponsePersonalisation;


    @BeforeEach
    public void setUp() throws NotificationClientException, IOException {
        Map<String, String> appelantInfo = new HashMap<>();
        appelantInfo.put("appellantGivenNames", appellantGivenNames);
        appelantInfo.put("appellantFamilyName", appellantFamilyName);
        appelantInfo.put("homeOfficeReferenceNumber", homeOfficeReferenceNumber);
        appelantInfo.put("appealReferenceNumber", appealReferenceNumber);

        when(personalisationProvider.getAppellantPersonalisation(asylumCase)).thenReturn(appelantInfo);
        when(asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class)).thenReturn(Optional.of(AppealReviewOutcome.DECISION_MAINTAINED));

        detentionEngagementTeamReviewHomeOfficeResponsePersonalisation =
                new DetentionEngagementTeamReviewHomeOfficeResponsePersonalisation(
                        templateId,
                        detEmailService,
                        documentDownloadClient,
                        subjectPrefix,
                        personalisationProvider
                );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, detentionEngagementTeamReviewHomeOfficeResponsePersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + detentionEngagementTeamReviewHomeOfficeResponseersonalisationReferenceId,
                detentionEngagementTeamReviewHomeOfficeResponsePersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detEmailService.getDetEmailAddress(asylumCase)).thenReturn(detEmailAddress);

        assertTrue(
                detentionEngagementTeamReviewHomeOfficeResponsePersonalisation.getRecipientsList(asylumCase).contains(detEmailAddress));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_no_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());
        assertEquals(Collections.emptySet(), detentionEngagementTeamReviewHomeOfficeResponsePersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_other_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        assertEquals(Collections.emptySet(), detentionEngagementTeamReviewHomeOfficeResponsePersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
                () -> detentionEngagementTeamReviewHomeOfficeResponsePersonalisation.getPersonalisationForLink((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_internal_detained_request_home_office_response_review_document_is_missing() {
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> detentionEngagementTeamReviewHomeOfficeResponsePersonalisation.getPersonalisationForLink(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("internalDetainedRequestHomeOfficeResponseReview document not available");
    }

    @ParameterizedTest
    @EnumSource(AppealReviewOutcome.class)
    public void should_return_correct_letter_based_on_appeal_review_outcome(AppealReviewOutcome appealReviewOutcome) throws NotificationClientException, IOException {
        IdValue<DocumentWithMetadata> documentIdValueMetaDataToUpload;
        documentIdValueMetaDataToUpload = appealReviewOutcome.equals(AppealReviewOutcome.DECISION_MAINTAINED)
                ? internalDetainedReviewHomeOfficeResponseDecisionMaintainedLetterId
                : internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterId;

        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(documentIdValueMetaDataToUpload)));

        DocumentWithMetadata documentToUpload;
        documentToUpload = appealReviewOutcome.equals(AppealReviewOutcome.DECISION_MAINTAINED)
                ? internalDetainedReviewHomeOfficeResponseDecisionMaintainedLetter
                : internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetter;

        when(documentDownloadClient.getJsonObjectFromDocument(documentToUpload)).thenReturn(jsonDocument);
        when(asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class)).thenReturn(Optional.of(appealReviewOutcome));

        String expectedDocumentName = appealReviewOutcome.equals(AppealReviewOutcome.DECISION_MAINTAINED)
                ? homeOfficeAppealReviewMaintainedDocumentName
                : homeOfficeAppealReviewWithdrawnDocumentName;

        final Map<String, Object> expectedPersonalisation =
                ImmutableMap
                        .<String, Object>builder()
                        .put("subjectPrefix", subjectPrefix)
                        .put("appealReferenceNumber", appealReferenceNumber)
                        .put("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
                        .put("appellantGivenNames", appellantGivenNames)
                        .put("appellantFamilyName", appellantFamilyName)
                        .put("documentName", expectedDocumentName)
                        .put("documentLink", jsonDocument)
                        .build();

        Map<String, Object> actualPersonalisation =
                detentionEngagementTeamReviewHomeOfficeResponsePersonalisation.getPersonalisationForLink(asylumCase);

        assertTrue(compareStringsAndJsonObjects(expectedPersonalisation, actualPersonalisation));
    }
}
