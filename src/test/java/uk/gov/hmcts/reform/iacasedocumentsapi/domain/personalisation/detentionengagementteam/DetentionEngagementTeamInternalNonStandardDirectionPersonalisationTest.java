package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import uk.gov.hmcts.reform.iacasedocumentsapi.TestUtils;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DetentionEmailService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HomeOfficeEmailFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
public class DetentionEngagementTeamInternalNonStandardDirectionPersonalisationTest {

    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    private final String nonAdaPrefix = "IAFT - SERVE IN PERSON";
    @Mock
    private DetentionEmailService detEmailService;
    @Mock
    HomeOfficeEmailFinder homeOfficeEmailFinder;

    private Long caseId = 12345L;
    private String templateId = "templateId";
    private String detEmailAddress = "detEmail@example.com";
    private final JSONObject jsonObject = new JSONObject("{\"title\": \"JsonDocument\"}");
    DocumentWithMetadata sendDirectionLetter = TestUtils.getDocumentWithMetadata(
            "id", "internal_appeal_submission", "some other desc", DocumentTag.INTERNAL_NON_STANDARD_DIRECTION_TO_APPELLANT_LETTER);
    IdValue<DocumentWithMetadata> document = new IdValue<>("1", sendDirectionLetter);

    @Mock
    DocumentDownloadClient documentDownloadClient;


    private DetentionEngagementTeamNonStandardDirectionPersonalisation detentionEngagementTeamNonStandardDirectionPersonalisation;

    @BeforeEach
    public void setup() throws NotificationClientException, IOException {
        detentionEngagementTeamNonStandardDirectionPersonalisation = new DetentionEngagementTeamNonStandardDirectionPersonalisation(
                templateId,
                detEmailService,
                documentDownloadClient,
                personalisationProvider
        );
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(document)));
        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(jsonObject);
    }

    @Test
    public void should_return_give_reference_id() {
        assertEquals(caseId + "_INTERNAL_NON_STANDARD_DIRECTION_DET",
                detentionEngagementTeamNonStandardDirectionPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, detentionEngagementTeamNonStandardDirectionPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_recipient_email_id() {
        when(detEmailService.getDetentionEmailAddress(asylumCase)).thenReturn(detEmailAddress);
        // Mock the appellant to be in detention
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_DETENTION, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
            .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES));
        assertEquals(Collections.singleton(detEmailAddress), detentionEngagementTeamNonStandardDirectionPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    void getRecipientsList_should_return_empty_set_when_not_in_detention() {
        // Mock the appellant to not be in detention
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_DETENTION, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
            .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO));

        assertEquals(Collections.emptySet(), detentionEngagementTeamNonStandardDirectionPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_given_personalisation() {
        ReflectionTestUtils.setField(detentionEngagementTeamNonStandardDirectionPersonalisation, "nonAdaPrefix", nonAdaPrefix);

        when(personalisationProvider.getAppellantPersonalisation(asylumCase)).thenReturn(getPersonalisation());

        Map<String, Object> personalisation =
                detentionEngagementTeamNonStandardDirectionPersonalisation.getPersonalisationForLink(callback);
        //assert the personalisation map values
        assertThat(personalisation).containsAllEntriesOf(getPersonalisation());
        assertEquals(jsonObject, personalisation.get("documentLink"));
        assertEquals(nonAdaPrefix, personalisation.get("subjectPrefix"));
    }

    @Test
    public void should_throw_exception_when_personalisation_when_callback_is_null() {

        assertThatThrownBy(() -> detentionEngagementTeamNonStandardDirectionPersonalisation.getPersonalisationForLink((AsylumCase) null))
                .hasMessage("asylumCase must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

    }

    private Map<String, String> getPersonalisation() {

        return ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", "PA/12345/001")
                .put("homeOfficeReference", "A1234567")
                .put("appellantGivenNames", "Talha")
                .put("appellantFamilyName", "Awan")
                .build();
    }
}
