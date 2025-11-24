package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.TestUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetentionEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionEngagementTeamEndAppealPersonalisationTest {
    @Mock
    AsylumCase asylumCase;
    @Mock
    private DocumentDownloadClient documentDownloadClient;
    @Mock
    private DetentionEmailService detentionEmailService;
    @Mock
    private PersonalisationProvider personalisationProvider;
    private String templateId = "templateId";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String adaPrefix = "ADA - SERVE IN PERSON";
    private final String nonAdaPrefix = "IAFT - SERVE IN PERSON";
    private final Long caseId = 12345L;
    final String adaFormName = "IAFT-ADA4: Make an application – Accelerated detained appeal (ADA)";
    final String nonAdaFormName = "IAFT-DE4: Make an application – Detained appeal";
    final String adaFormLink = "https://www.gov.uk/government/publications/make-an-application-accelerated-detained-appeal-form-iaft-ada4";
    final String nonAdaFormLink = "https://www.gov.uk/government/publications/make-an-application-detained-appeal-form-iaft-de4";
    private DetentionEngagementTeamEndAppealPersonalisation detentionEngagementTeamEndAppealPersonalisation;

    private final JSONObject jsonObject = new JSONObject("{\"title\": \"JsonDocument\"}");
    DocumentWithMetadata endAppealDoc = TestUtils.getDocumentWithMetadata(
            "id", "internal_end_appeal", "some other desc", DocumentTag.END_APPEAL);
    IdValue<DocumentWithMetadata> endAppealBundle = new IdValue<>("1", endAppealDoc);

    DetentionEngagementTeamEndAppealPersonalisationTest() {
    }

    @BeforeEach
    public void setup() throws NotificationClientException, IOException {
        detentionEngagementTeamEndAppealPersonalisation = new DetentionEngagementTeamEndAppealPersonalisation(
                templateId,
                detentionEmailService,
                personalisationProvider,
                documentDownloadClient
        );

        ReflectionTestUtils.setField(detentionEngagementTeamEndAppealPersonalisation, "adaSubjectPrefix", "ADA - SERVE IN PERSON");
        ReflectionTestUtils.setField(detentionEngagementTeamEndAppealPersonalisation, "nonAdaPrefix", "IAFT - SERVE IN PERSON");
        Map<String, String> appelantInfo = new HashMap<>();
        appelantInfo.put("appellantGivenNames", appellantGivenNames);
        appelantInfo.put("appellantFamilyName", appellantFamilyName);
        appelantInfo.put("homeOfficeReferenceNumber", homeOfficeReferenceNumber);
        appelantInfo.put("appealReferenceNumber", appealReferenceNumber);

        when(personalisationProvider.getAppellantPersonalisation(asylumCase)).thenReturn(appelantInfo);
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(endAppealBundle)));
        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(jsonObject);
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_INTERNAL_DET_END_APPEAL_EMAIL",
                detentionEngagementTeamEndAppealPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_det_email_address() {
        String detentionEngagementTeamEmail = "det@email.com";
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detentionEmailService.getDetentionEmailAddress(asylumCase)).thenReturn(detentionEngagementTeamEmail);

        assertTrue(
                detentionEngagementTeamEndAppealPersonalisation.getRecipientsList(asylumCase).contains(detentionEngagementTeamEmail)
        );
    }

    @Test
    void should_throw_exception_when_no_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());
        when(detentionEmailService.getDetentionEmailAddress(asylumCase)).thenThrow(new IllegalStateException("Detention facility is not present"));

        assertThatThrownBy(() -> detentionEngagementTeamEndAppealPersonalisation.getRecipientsList(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Detention facility is not present");
    }

    @Test
    void should_throw_exception_when_other_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        when(detentionEmailService.getDetentionEmailAddress(asylumCase)).thenThrow(new IllegalStateException("Detention facility is not valid"));

        assertThatThrownBy(() -> detentionEngagementTeamEndAppealPersonalisation.getRecipientsList(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Detention facility is not valid");
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> detentionEngagementTeamEndAppealPersonalisation.getPersonalisationForLink((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @ParameterizedTest
    @EnumSource(YesOrNo.class)
    void should_return_personalisation_if_decision_dismissed_for_ada(YesOrNo yesOrNo) throws NotificationClientException, IOException {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(yesOrNo));
        Map<String, Object> personalisation = detentionEngagementTeamEndAppealPersonalisation.getPersonalisationForLink(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(jsonObject, personalisation.get("documentLink"));

        if (yesOrNo == YesOrNo.YES) {
            assertEquals(adaPrefix, personalisation.get("subjectPrefix"));
        } else {
            assertEquals(nonAdaPrefix, personalisation.get("subjectPrefix"));
        }
    }

}