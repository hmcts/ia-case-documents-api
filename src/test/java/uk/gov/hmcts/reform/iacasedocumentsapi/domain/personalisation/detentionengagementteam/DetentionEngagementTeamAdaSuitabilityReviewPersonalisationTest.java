package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.TestUtils.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.*;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam.DetentionEngagementTeamAdaSuitabilityReviewPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DetentionEngagementTeamAdaSuitabilityReviewPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    DetEmailService detEmailService;
    @Mock
    JSONObject jsonDocument;
    @Mock
    DocumentDownloadClient documentDownloadClient;

    private final String templateId = "someTemplateId";
    private final String internalAdaSuitabilityReviewPersonalisationReferenceId = "_ADA_SUITABILITY_DETERMINED_INTERNAL_ADA_DET";
    private final String detEmailAddress = "some@example.com";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String adaPrefix = "ADA - SERVE IN PERSON";
    DocumentWithMetadata internalAdaSuitabilityLetter = getDocumentWithMetadata(
        "1", "ADA-Appellant-letter-suitability-decision-suitable", "some other desc", DocumentTag.INTERNAL_ADA_SUITABILITY);
    IdValue<DocumentWithMetadata> internalAdaSuitabilityLetterId = new IdValue<>("1", internalAdaSuitabilityLetter);
    private DetentionEngagementTeamAdaSuitabilityReviewPersonalisation detentionEngagementTeamAdaSuitabilityReviewPersonalisation;


    @BeforeEach
    public void setUp() throws NotificationClientException, IOException {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));

        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(internalAdaSuitabilityLetterId)));
        when(documentDownloadClient.getJsonObjectFromDocument(internalAdaSuitabilityLetter)).thenReturn(jsonDocument);

        detentionEngagementTeamAdaSuitabilityReviewPersonalisation =
            new DetentionEngagementTeamAdaSuitabilityReviewPersonalisation(
                templateId,
                detEmailService,
                documentDownloadClient,
                adaPrefix
            );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, detentionEngagementTeamAdaSuitabilityReviewPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + internalAdaSuitabilityReviewPersonalisationReferenceId,
            detentionEngagementTeamAdaSuitabilityReviewPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detEmailService.getRecipientsList(asylumCase)).thenReturn(Collections.singleton(detEmailAddress));

        assertTrue(
            detentionEngagementTeamAdaSuitabilityReviewPersonalisation.getRecipientsList(asylumCase).contains(detEmailAddress));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_no_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());
        assertEquals(Collections.emptySet(), detentionEngagementTeamAdaSuitabilityReviewPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_other_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        assertEquals(Collections.emptySet(), detentionEngagementTeamAdaSuitabilityReviewPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> detentionEngagementTeamAdaSuitabilityReviewPersonalisation.getPersonalisationForLink((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_internal_ada_suitability_review_document_is_missing() {
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> detentionEngagementTeamAdaSuitabilityReviewPersonalisation.getPersonalisationForLink(asylumCase))
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessage("Internal ADA Suitability document is not present");
    }

    @Test
    public void should_return_personalisation_when_all_information_given_refused() {

        final Map<String, Object> expectedPersonalisation =
            ImmutableMap
                .<String, Object>builder()
                .put("subjectPrefix", adaPrefix)
                .put("appealReferenceNumber", appealReferenceNumber)
                .put("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantFamilyName)
                .put("documentLink", jsonDocument)
                .build();

        Map<String, Object> actualPersonalisation =
            detentionEngagementTeamAdaSuitabilityReviewPersonalisation.getPersonalisationForLink(asylumCase);

        assertTrue(compareStringsAndJsonObjects(expectedPersonalisation, actualPersonalisation));
    }
}
