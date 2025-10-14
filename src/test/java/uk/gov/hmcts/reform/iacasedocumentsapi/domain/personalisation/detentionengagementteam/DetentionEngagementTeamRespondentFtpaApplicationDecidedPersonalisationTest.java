package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.TestUtils.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.*;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.*;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam.DetentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.TestUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DetentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisationTest {

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
    private String grantedTemplateId = "someGrantedTemplateId";
    private String refusedTemplateId = "someRefusedTemplateId";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String adaPrefix = "ADA - SERVE IN PERSON";
    private final String nonAdaPrefix = "IAFT - SERVE IN PERSON";
    private final Long caseId = 12345L;

    private DetentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation detentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation;

    DocumentWithMetadata doc = TestUtils.getDocumentWithMetadata(
        "id", "internal-detained-ho-ftpa-decided-letter", "some other desc", DocumentTag.INTERNAL_HO_FTPA_DECIDED_LETTER);
    IdValue<DocumentWithMetadata> bundle = new IdValue<>("1", doc);

    DetentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisationTest() {
    }

    @BeforeEach
    public void setup() throws NotificationClientException, IOException {
        detentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation = new DetentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation(
            grantedTemplateId,
            refusedTemplateId,
            detEmailService,
            personalisationProvider,
            documentDownloadClient
        );

        Map<String, String> appelantInfo = new HashMap<>();
        appelantInfo.put("appellantGivenNames", appellantGivenNames);
        appelantInfo.put("appellantFamilyName", appellantFamilyName);
        appelantInfo.put("homeOfficeReferenceNumber", homeOfficeReferenceNumber);
        appelantInfo.put("appealReferenceNumber", appealReferenceNumber);

        ReflectionTestUtils.setField(detentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation, "adaSubjectPrefix", "ADA - SERVE IN PERSON");
        ReflectionTestUtils.setField(detentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation, "nonAdaPrefix", "IAFT - SERVE IN PERSON");

        when(personalisationProvider.getAppellantPersonalisation(asylumCase)).thenReturn(appelantInfo);
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(bundle)));
        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(jsonDocument);
    }

    @ParameterizedTest
    @EnumSource(value = FtpaDecisionOutcomeType.class, names = {"FTPA_GRANTED", "FTPA_PARTIALLY_GRANTED", "FTPA_REFUSED"})
    void should_return_given_template_id_detained(FtpaDecisionOutcomeType ftpaDecisionOutcomeType) {
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(ftpaDecisionOutcomeType));

        assertEquals(
            ftpaDecisionOutcomeType.equals(FTPA_GRANTED) || ftpaDecisionOutcomeType.equals(FTPA_PARTIALLY_GRANTED)
                ? grantedTemplateId
                : refusedTemplateId, detentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation.getTemplateId(asylumCase)
        );
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_INTERNAL_DET_HO_FTPA_DECIDED_EMAIL",
            detentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_det_email_address() {
        String detentionEngagementTeamEmail = "det@email.com";
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detEmailService.getRecipientsList(asylumCase)).thenReturn(Collections.singleton(detentionEngagementTeamEmail));

        assertTrue(
            detentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation.getRecipientsList(asylumCase).contains(detentionEngagementTeamEmail));
    }

    @Test
    void should_return_empty_set_email_address_from_asylum_case_no_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());
        assertEquals(Collections.emptySet(), detentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    void should_return_empty_set_email_address_from_asylum_case_other_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        assertEquals(Collections.emptySet(), detentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> detentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation.getPersonalisationForLink((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_throw_exception_when_appeal_submission_is_empty() {
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> detentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation.getPersonalisationForLink(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("internalHoFtpaDecidedLetter document not available");
    }

    @Test
    void should_throw_exception_when_notification_client_throws_Exception() throws NotificationClientException, IOException {
        when(documentDownloadClient.getJsonObjectFromDocument(doc)).thenThrow(new NotificationClientException("File size is more than 2MB"));
        assertThatThrownBy(() -> detentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation.getPersonalisationForLink(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Failed to get HO Ftpa decision Letter in compatible format");
    }

    @ParameterizedTest
    @EnumSource(YesOrNo.class)
    void should_return_personalisation_when_all_information_given(YesOrNo yesOrNo) throws NotificationClientException, IOException {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.ofNullable(yesOrNo));

        final Map<String, Object> expectedPersonalisation =
            ImmutableMap
                .<String, Object>builder()
                .put("subjectPrefix", yesOrNo == YesOrNo.YES ? adaPrefix : nonAdaPrefix)
                .put("appealReferenceNumber", appealReferenceNumber)
                .put("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantFamilyName)
                .put("documentLink", jsonDocument)
                .build();

        Map<String, Object> actualPersonalisation =
            detentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation.getPersonalisationForLink(asylumCase);

        assertTrue(compareStringsAndJsonObjects(expectedPersonalisation, actualPersonalisation));
    }
}
