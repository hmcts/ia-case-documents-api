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
import java.util.List;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.TestUtils;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealReviewOutcome;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DetentionEngagementTeamUploadAppealResponsePersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    DetEmailService detEmailService;
    @Mock
    DocumentDownloadClient documentDownloadClient;

    private final String templateId = "someTemplateId";
    private final String adaPrefix = "Accelerated detained appeal";
    private final String detEmailAddress = "legalrep@example.com";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String listingReference = "listingReference";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";

    DocumentWithMetadata appealResponseLetter = getDocumentWithMetadata(
            "1", "ADA-Appellant-letter-suitability-decision-suitable", "some other desc", DocumentTag.UPLOAD_THE_APPEAL_RESPONSE);
    IdValue<DocumentWithMetadata> appealResponseLetterId = new IdValue<>("1", appealResponseLetter);
    private JSONObject appealResponseJsonDocument;

    private DetentionEngagementTeamUploadAppealResponsePersonalisation
        detentionEngagementTeamUploadAppealResponsePersonalisation;

    @BeforeEach
    public void setUp() throws NotificationClientException, IOException {

        when(detEmailService.getDetEmailAddress(asylumCase)).thenReturn(detEmailAddress);
        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(appealResponseJsonDocument);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(listingReference));
        when(asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class))
            .thenReturn(Optional.of(AppealReviewOutcome.DECISION_WITHDRAWN));

        String customerServicesTelephone = "555 555 555";
        String customerServicesEmail = "customer.services@example.com";
        when(customerServicesProvider.getCustomerServicesTelephone()).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getCustomerServicesEmail()).thenReturn(customerServicesEmail);

        String hearingDate = "2023-03-15T10:13:38.410992";
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDate));

        List<IdValue<DocumentWithMetadata>> appealResponseDocuments = TestUtils.getDocumentWithMetadataList("docId", "filename", "description", DocumentTag.APPEAL_RESPONSE);
        appealResponseJsonDocument =  new JSONObject("{\"title\": \"Home Office Response JsonDocument\"}");
        when(asylumCase.read(RESPONDENT_DOCUMENTS)).thenReturn(Optional.of(appealResponseDocuments));
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(appealResponseLetterId)));
        when(documentDownloadClient.getJsonObjectFromDocument(appealResponseLetter)).thenReturn(appealResponseJsonDocument);

        detentionEngagementTeamUploadAppealResponsePersonalisation =
            new DetentionEngagementTeamUploadAppealResponsePersonalisation(
                templateId,
                adaPrefix,
                customerServicesProvider,
                detEmailService,
                documentDownloadClient
            );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, detentionEngagementTeamUploadAppealResponsePersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_UPLOADED_HO_RESPONSE_DETENTION_ENGAGEMENT_TEAM",
            detentionEngagementTeamUploadAppealResponsePersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        String detentionEngagementTeamEmail = "det@email.com";
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detEmailService.getRecipientsList(asylumCase)).thenReturn(Collections.singleton(detentionEngagementTeamEmail));

        assertTrue(
            detentionEngagementTeamUploadAppealResponsePersonalisation.getRecipientsList(asylumCase).contains(detentionEngagementTeamEmail));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_no_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());
        assertEquals(Collections.emptySet(), detentionEngagementTeamUploadAppealResponsePersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_other_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        assertEquals(Collections.emptySet(), detentionEngagementTeamUploadAppealResponsePersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> detentionEngagementTeamUploadAppealResponsePersonalisation.getPersonalisationForLink((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_appeal_review_outcome_is_missing() {
        when(asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> detentionEngagementTeamUploadAppealResponsePersonalisation.getPersonalisationForLink(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Appeal review outcome is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_appeal_response_document_is_missing() {
        when(asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class))
                .thenReturn(Optional.of(AppealReviewOutcome.DECISION_MAINTAINED));

        DocumentWithMetadata letter = getDocumentWithMetadata(
                "1", "ADA-Appellant-letter-suitability-decision-suitable", "some other desc", DocumentTag.ADA_SUITABILITY);
        IdValue<DocumentWithMetadata> adaLetter = new IdValue<>("1", letter);
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(adaLetter)));

        assertThatThrownBy(
            () -> detentionEngagementTeamUploadAppealResponsePersonalisation.getPersonalisationForLink(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Appeal response letter not available");
    }

    @Test
    public void should_return_personalisation_when_all_information_given_maintain() throws NotificationClientException, IOException {

        when(asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class))
            .thenReturn(Optional.of(AppealReviewOutcome.DECISION_MAINTAINED));

        final Map<String, Object> expectedPersonalisation =
            ImmutableMap
                .<String, Object>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("subjectPrefix", adaPrefix)
                .put("appealReferenceNumber", appealReferenceNumber)
                .put("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantFamilyName)
                .put("documentName", "Home Office Response")
                .put("documentLink", appealResponseJsonDocument)
                .build();

        Map<String, Object> actualPersonalisation =
            detentionEngagementTeamUploadAppealResponsePersonalisation.getPersonalisationForLink(asylumCase);

        assertTrue(compareStringsAndJsonObjects(expectedPersonalisation, actualPersonalisation));
    }

    @Test
    public void should_return_personalisation_when_all_information_given_withdrawn() throws NotificationClientException, IOException {

        when(asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class))
            .thenReturn(Optional.of(AppealReviewOutcome.DECISION_WITHDRAWN));

        final Map<String, Object> expectedPersonalisation =
            ImmutableMap
                .<String, Object>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("subjectPrefix", adaPrefix)
                .put("appealReferenceNumber", appealReferenceNumber)
                .put("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantFamilyName)
                .put("documentName", "Withdrawal Letter")
                .put("documentLink", new JSONObject())
                .build();

        Map<String, Object> actualPersonalisation =
            detentionEngagementTeamUploadAppealResponsePersonalisation.getPersonalisationForLink(asylumCase);

        assertTrue(compareStringsAndJsonObjects(expectedPersonalisation, actualPersonalisation));
    }
}
