package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.TestUtils.compareStringsAndJsonObjects;
import static uk.gov.hmcts.reform.iacasedocumentsapi.TestUtils.getDocumentWithMetadata;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixesForInternalAppealByPost;

import java.io.IOException;
import java.util.Collections;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionEngagementTeamMaintainCaseUnlinkAppealPersonalisationTest {

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
    private final String personalisationReferenceId = "_INTERNAL_DET_MAINTAIN_CASE_UNLINK_APPEAL_EMAIL";
    private final String detEmailAddress = "some@example.com";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "someReferenceNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String adaPrefix = "ADA - SERVE BY POST";
    private final String nonAdaPrefix = "IAFT - SERVE BY POST";
    DocumentWithMetadata internalMaintainCaseUnlinkAppealLetter = getDocumentWithMetadata(
            "1", "Maintain case unlink letter", "some other desc", DocumentTag.MAINTAIN_CASE_UNLINK_APPEAL_LETTER);
    IdValue<DocumentWithMetadata> internalMaintainCaseLinksLetterId = new IdValue<>("1", internalMaintainCaseUnlinkAppealLetter);
    private DetentionEngagementTeamMaintainCaseUnlinkAppealPersonalisation detentionEngagementTeamMaintainCaseUnlinkAppealPersonalisation;

    @BeforeEach
    public void setUp() throws NotificationClientException, IOException {
        Map<String, String> appelantInfo = new HashMap<>();
        appelantInfo.put("appellantGivenNames", appellantGivenNames);
        appelantInfo.put("appellantFamilyName", appellantFamilyName);
        appelantInfo.put("homeOfficeReferenceNumber", homeOfficeReferenceNumber);
        appelantInfo.put("appealReferenceNumber", appealReferenceNumber);

        when(personalisationProvider.getAppellantPersonalisation(asylumCase)).thenReturn(appelantInfo);
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(internalMaintainCaseLinksLetterId)));
        when(documentDownloadClient.getJsonObjectFromDocument(internalMaintainCaseUnlinkAppealLetter)).thenReturn(jsonDocument);

        detentionEngagementTeamMaintainCaseUnlinkAppealPersonalisation =
                new DetentionEngagementTeamMaintainCaseUnlinkAppealPersonalisation(
                        templateId,
                        detEmailService,
                        documentDownloadClient,
                        personalisationProvider,
                        adaPrefix,
                        nonAdaPrefix
                );

        initializePrefixesForInternalAppealByPost(detentionEngagementTeamMaintainCaseUnlinkAppealPersonalisation);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, detentionEngagementTeamMaintainCaseUnlinkAppealPersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + personalisationReferenceId,
                detentionEngagementTeamMaintainCaseUnlinkAppealPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_asylum_case() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detEmailService.getRecipientsList(asylumCase)).thenReturn(Collections.singleton(detEmailAddress));

        assertTrue(
                detentionEngagementTeamMaintainCaseUnlinkAppealPersonalisation.getRecipientsList(asylumCase).contains(detEmailAddress));
    }

    @Test
    void getRecipientsList_should_return_empty_set_if_not_in_detention() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertEquals(Collections.emptySet(), detentionEngagementTeamMaintainCaseUnlinkAppealPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    void should_return_empty_set_email_address_from_asylum_case_no_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());
        assertEquals(Collections.emptySet(), detentionEngagementTeamMaintainCaseUnlinkAppealPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    void should_return_empty_set_email_address_from_asylum_case_other_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        assertEquals(Collections.emptySet(), detentionEngagementTeamMaintainCaseUnlinkAppealPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(
                () -> detentionEngagementTeamMaintainCaseUnlinkAppealPersonalisation.getPersonalisationForLink((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_throw_exception_on_personalisation_when_internal_maintain_case_links_document_is_missing() {
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> detentionEngagementTeamMaintainCaseUnlinkAppealPersonalisation.getPersonalisationForLink(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("maintainCaseUnlinkAppealLetter document not available");
    }

    @ParameterizedTest
    @EnumSource(YesOrNo.class)
    void should_return_correct_personalisation_for_detained_ada_and_non_ada_case(YesOrNo yesOrNo) {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(yesOrNo));

        final Map<String, Object> expectedPersonalisation = new HashMap<>();
        expectedPersonalisation.put("appealReferenceNumber", appealReferenceNumber);
        expectedPersonalisation.put("homeOfficeReferenceNumber", homeOfficeReferenceNumber);
        expectedPersonalisation.put("appellantGivenNames", appellantGivenNames);
        expectedPersonalisation.put("appellantFamilyName", appellantFamilyName);
        expectedPersonalisation.put("documentLink", jsonDocument);

        if (yesOrNo.equals(YesOrNo.YES)) {
            expectedPersonalisation.put("subjectPrefix", adaPrefix);
        } else {
            expectedPersonalisation.put("subjectPrefix", nonAdaPrefix);
        }

        Map<String, Object> actualPersonalisation =
                detentionEngagementTeamMaintainCaseUnlinkAppealPersonalisation.getPersonalisationForLink(asylumCase);

        assertTrue(compareStringsAndJsonObjects(expectedPersonalisation, actualPersonalisation));
    }
}
