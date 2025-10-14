package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.TestUtils.compareStringsAndJsonObjects;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam.DetentionEngagementApplyForFtpaRespondentPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.TestUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionEngagementApplyForFtpaRespondentPersonalisationTest {
    @Mock
    AsylumCase asylumCase;
    @Mock
    private DocumentDownloadClient documentDownloadClient;
    @Mock
    private DetEmailService detEmailService;
    @Mock
    private PersonalisationProvider personalisationProvider;
    @Mock
    JSONObject jsonDocument;
    private String templateId = "templateId";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String adaPrefix = "ADA - SERVE IN PERSON";
    private final String nonAdaPrefix = "IAFT - SERVE IN PERSON";
    private final Long caseId = 12345L;
    private DetentionEngagementApplyForFtpaRespondentPersonalisation detentionEngagementApplyForFtpaRespondentPersonalisation;

    DocumentWithMetadata caseListedDoc = TestUtils.getDocumentWithMetadata(
            "id", "internal-detained-apply-for-ftpa-respondent-letter", "some other desc", DocumentTag.INTERNAL_APPLY_FOR_FTPA_RESPONDENT);
    IdValue<DocumentWithMetadata> caseListedBundle = new IdValue<>("1", caseListedDoc);

    DetentionEngagementApplyForFtpaRespondentPersonalisationTest() {
    }

    @BeforeEach
    public void setup() throws NotificationClientException, IOException {
        Map<String, String> appelantInfo = new HashMap<>();
        appelantInfo.put("appellantGivenNames", appellantGivenNames);
        appelantInfo.put("appellantFamilyName", appellantFamilyName);
        appelantInfo.put("homeOfficeReferenceNumber", homeOfficeReferenceNumber);
        appelantInfo.put("appealReferenceNumber", appealReferenceNumber);

        when(personalisationProvider.getAppellantPersonalisation(asylumCase)).thenReturn(appelantInfo);
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(caseListedBundle)));
        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(jsonDocument);

        detentionEngagementApplyForFtpaRespondentPersonalisation = new DetentionEngagementApplyForFtpaRespondentPersonalisation(
                templateId,
                documentDownloadClient,
                detEmailService,
                personalisationProvider,
                adaPrefix,
                nonAdaPrefix
        );
    }

    @Test
    void should_return_given_template_id_detained() {
        assertEquals(
                templateId,
                detentionEngagementApplyForFtpaRespondentPersonalisation.getTemplateId()
        );
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_INTERNAL_DETAINED_APPLY_FOR_FTPA_RESPONDENT",
                detentionEngagementApplyForFtpaRespondentPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_det_email_address() {
        String detentionEngagementTeamEmail = "det@email.com";
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detEmailService.getRecipientsList(asylumCase)).thenReturn(Collections.singleton(detentionEngagementTeamEmail));

        assertTrue(
                detentionEngagementApplyForFtpaRespondentPersonalisation.getRecipientsList(asylumCase).contains(detentionEngagementTeamEmail));
    }

    @Test
    void getRecipientsList_should_return_empty_set_if_not_in_detention() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertEquals(Collections.emptySet(), detentionEngagementApplyForFtpaRespondentPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    void should_return_empty_set_email_address_from_asylum_case_no_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());
        assertEquals(Collections.emptySet(), detentionEngagementApplyForFtpaRespondentPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    void should_return_empty_set_email_address_from_asylum_case_other_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        assertEquals(Collections.emptySet(), detentionEngagementApplyForFtpaRespondentPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

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
                detentionEngagementApplyForFtpaRespondentPersonalisation.getPersonalisationForLink(asylumCase);

        assertTrue(compareStringsAndJsonObjects(expectedPersonalisation, actualPersonalisation));
    }

    @Test
    void should_return_personalisation_if_decision_dismissed_for_nonAda() throws NotificationClientException, IOException {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YES));
        Map<String, Object> personalisation = detentionEngagementApplyForFtpaRespondentPersonalisation.getPersonalisationForLink(asylumCase);

        assertEquals(adaPrefix, personalisation.get("subjectPrefix"));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> detentionEngagementApplyForFtpaRespondentPersonalisation.getPersonalisationForLink((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_throw_exception_when_appeal_submission_is_empty() {
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> detentionEngagementApplyForFtpaRespondentPersonalisation.getPersonalisationForLink(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("internalApplyForFtpaRespondent document not available");
    }

    @Test
    void should_throw_exception_when_notification_client_throws_Exception() throws NotificationClientException, IOException {
        when(documentDownloadClient.getJsonObjectFromDocument(caseListedDoc)).thenThrow(new NotificationClientException("File size is more than 2MB"));
        assertThatThrownBy(() -> detentionEngagementApplyForFtpaRespondentPersonalisation.getPersonalisationForLink(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to get Internal detained apply for FTPA respondent letter in compatible format");
    }

}
