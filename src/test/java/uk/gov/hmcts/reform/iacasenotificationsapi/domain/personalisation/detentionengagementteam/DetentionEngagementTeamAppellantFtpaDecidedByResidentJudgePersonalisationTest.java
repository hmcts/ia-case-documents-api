package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.TestUtils.compareStringsAndJsonObjects;
import static uk.gov.hmcts.reform.iacasenotificationsapi.TestUtils.getDocumentWithMetadata;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixesForInternalAppeal;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DetentionEngagementTeamAppellantFtpaDecidedByResidentJudgePersonalisationTest {

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

    private final String appellantFtpaDecidedByResidentJudgeTemplateId = "someTemplateId";
    private final String detentionEngagementTeamAppellantFtpaDecidedByResidentJudgePersonalisationReferenceId = "_INTERNAL_APPELLANT_FTPA_DECIDED_BY_RESIDENT_JUDGE_DET";
    private final String detEmailAddress = "some@example.com";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "someReferenceNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String adaPrefix = "ADA - SERVE IN PERSON";
    private final String nonAdaPrefix = "IAFT - SERVE IN PERSON";
    private final String iaut1FormUrl = "https://www.gov.uk/government/publications/form-iaut1-application-for-permission-to-appeal-from-first-tier-tribunal";
    private final String formLinkForTemplateIfRequired = "*IAUT1: Application for permission to appeal from First-tier Tribunal\n" + iaut1FormUrl;
    DocumentWithMetadata internalFtpaDecidedByRjLetter = getDocumentWithMetadata(
            "1", "FTPA decided by resident judge letter", "some other desc", DocumentTag.INTERNAL_APPELLANT_FTPA_DECIDED_LETTER);
    IdValue<DocumentWithMetadata> internalFtpaDecidedByRjLetterId = new IdValue<>("1", internalFtpaDecidedByRjLetter);
    private DetentionEngagementTeamAppellantFtpaDecidedByResidentJudgePersonalisation detentionEngagementTeamAppellantFtpaDecidedByResidentJudgePersonalisation;

    @BeforeEach
    public void setUp() throws NotificationClientException, IOException {
        Map<String, String> appelantInfo = new HashMap<>();
        appelantInfo.put("appellantGivenNames", appellantGivenNames);
        appelantInfo.put("appellantFamilyName", appellantFamilyName);
        appelantInfo.put("homeOfficeReferenceNumber", homeOfficeReferenceNumber);
        appelantInfo.put("appealReferenceNumber", appealReferenceNumber);

        when(personalisationProvider.getAppellantPersonalisation(asylumCase)).thenReturn(appelantInfo);

        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(FTPA_GRANTED));

        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(internalFtpaDecidedByRjLetterId)));
        when(documentDownloadClient.getJsonObjectFromDocument(internalFtpaDecidedByRjLetter)).thenReturn(jsonDocument);

        detentionEngagementTeamAppellantFtpaDecidedByResidentJudgePersonalisation =
                new DetentionEngagementTeamAppellantFtpaDecidedByResidentJudgePersonalisation(
                        appellantFtpaDecidedByResidentJudgeTemplateId,
                        detEmailService,
                        documentDownloadClient,
                        adaPrefix,
                        nonAdaPrefix,
                        personalisationProvider
                );

        initializePrefixesForInternalAppeal(detentionEngagementTeamAppellantFtpaDecidedByResidentJudgePersonalisation);
        ReflectionTestUtils.setField(detentionEngagementTeamAppellantFtpaDecidedByResidentJudgePersonalisation, "iaut1FormUrl", iaut1FormUrl);

    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(appellantFtpaDecidedByResidentJudgeTemplateId, detentionEngagementTeamAppellantFtpaDecidedByResidentJudgePersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + detentionEngagementTeamAppellantFtpaDecidedByResidentJudgePersonalisationReferenceId,
                detentionEngagementTeamAppellantFtpaDecidedByResidentJudgePersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detEmailService.getDetEmailAddress(asylumCase)).thenReturn(detEmailAddress);

        assertTrue(
                detentionEngagementTeamAppellantFtpaDecidedByResidentJudgePersonalisation.getRecipientsList(asylumCase).contains(detEmailAddress));
    }

    @Test
    void getRecipientsList_should_return_empty_set_if_not_in_detention() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertEquals(Collections.emptySet(), detentionEngagementTeamAppellantFtpaDecidedByResidentJudgePersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_no_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());
        assertEquals(Collections.emptySet(), detentionEngagementTeamAppellantFtpaDecidedByResidentJudgePersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_other_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        assertEquals(Collections.emptySet(), detentionEngagementTeamAppellantFtpaDecidedByResidentJudgePersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(
                () -> detentionEngagementTeamAppellantFtpaDecidedByResidentJudgePersonalisation.getPersonalisationForLink((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_internal_ftpa_decided_document_is_missing() {
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> detentionEngagementTeamAppellantFtpaDecidedByResidentJudgePersonalisation.getPersonalisationForLink(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("internalAppellantFtpaDecidedLetter document not available");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_ftpa_decision_is_missing() {
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> detentionEngagementTeamAppellantFtpaDecidedByResidentJudgePersonalisation.getPersonalisationForLink(asylumCase))
                .isExactlyInstanceOf(RequiredFieldMissingException.class)
                .hasMessage("FTPA decision not found");
    }

    @ParameterizedTest
    @EnumSource(value = FtpaDecisionOutcomeType.class, names = {"FTPA_PARTIALLY_GRANTED", "FTPA_REFUSED", "FTPA_GRANTED"})
    public void should_return_correct_personalisation_for_detained_non_ada_case(FtpaDecisionOutcomeType ftpaDecisionOutcomeType) {

        final Map<String, Object> expectedPersonalisation = new HashMap<>();
        expectedPersonalisation.put("subjectPrefix", nonAdaPrefix);
        expectedPersonalisation.put("appealReferenceNumber", appealReferenceNumber);
        expectedPersonalisation.put("homeOfficeReferenceNumber", homeOfficeReferenceNumber);
        expectedPersonalisation.put("appellantGivenNames", appellantGivenNames);
        expectedPersonalisation.put("appellantFamilyName", appellantFamilyName);
        expectedPersonalisation.put("documentLink", jsonDocument);

        if (List.of(FTPA_PARTIALLY_GRANTED, FTPA_REFUSED).contains(ftpaDecisionOutcomeType)) {
            expectedPersonalisation.put("formLinkForTemplateIfRequired", formLinkForTemplateIfRequired);
        } else {
            expectedPersonalisation.put("formLinkForTemplateIfRequired", "");
        }

        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(ftpaDecisionOutcomeType));

        Map<String, Object> actualPersonalisation =
                detentionEngagementTeamAppellantFtpaDecidedByResidentJudgePersonalisation.getPersonalisationForLink(asylumCase);

        assertTrue(compareStringsAndJsonObjects(expectedPersonalisation, actualPersonalisation));
    }

    @ParameterizedTest
    @EnumSource(value = FtpaDecisionOutcomeType.class, names = {"FTPA_PARTIALLY_GRANTED", "FTPA_REFUSED", "FTPA_GRANTED"})
    public void should_return_correct_personalisation_for_ada_case(FtpaDecisionOutcomeType ftpaDecisionOutcomeType) {

        final Map<String, Object> expectedPersonalisation = new HashMap<>();
        expectedPersonalisation.put("subjectPrefix", adaPrefix);
        expectedPersonalisation.put("appealReferenceNumber", appealReferenceNumber);
        expectedPersonalisation.put("homeOfficeReferenceNumber", homeOfficeReferenceNumber);
        expectedPersonalisation.put("appellantGivenNames", appellantGivenNames);
        expectedPersonalisation.put("appellantFamilyName", appellantFamilyName);
        expectedPersonalisation.put("documentLink", jsonDocument);

        if (List.of(FTPA_PARTIALLY_GRANTED, FTPA_REFUSED).contains(ftpaDecisionOutcomeType)) {
            expectedPersonalisation.put("formLinkForTemplateIfRequired", formLinkForTemplateIfRequired);
        } else {
            expectedPersonalisation.put("formLinkForTemplateIfRequired", "");
        }

        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(ftpaDecisionOutcomeType));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        Map<String, Object> actualPersonalisation =
                detentionEngagementTeamAppellantFtpaDecidedByResidentJudgePersonalisation.getPersonalisationForLink(asylumCase);

        assertTrue(compareStringsAndJsonObjects(expectedPersonalisation, actualPersonalisation));
    }
}
