package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.TestUtils.getDocumentWithMetadata;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixesForInternalAppeal;

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
public class DetentionEngagementTeamLegalOfficerUploadAddendumEvidenceTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    DetEmailService detEmailService;
    @Mock
    JSONObject jsonDocument;
    @Mock
    DocumentDownloadClient documentDownloadClient;
    @Mock
    PersonalisationProvider personalisationProvider;
    private String templateId = "templateId";
    private final String uploadAdditionalEvidencePersonalisationReferenceId = "_INTERNAL_DETAINED_LEGAL_OFFICER_UPLOAD_ADDITIONAL_EVIDENCE_DET_EMAIL";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String detEmailAddress = "some@example.com";
    private final String adaPrefix = "ADA - SERVE IN PERSON";
    private final String nonAdaPrefix = "IAFT - SERVE IN PERSON";
    private final Long caseId = 12345L;
    private DetentionEngagementTeamLegalOfficerUploadAddendumEvidence detentionEngagementTeamLegalOfficerUploadAddendumEvidence;
    DocumentWithMetadata uploadAdditionalEvidenceDoc = getDocumentWithMetadata(
            "1", "appellant letter_LO-evidence", "some other desc", DocumentTag.LEGAL_OFFICER_UPLOAD_ADDITIONAL_EVIDENCE_LETTER);
    IdValue<DocumentWithMetadata> uploadAdditionalEvidenceDocId = new IdValue<>("1", uploadAdditionalEvidenceDoc);

    DetentionEngagementTeamLegalOfficerUploadAddendumEvidenceTest() {
    }

    @BeforeEach
    void setup() throws NotificationClientException, IOException {
        detentionEngagementTeamLegalOfficerUploadAddendumEvidence = new DetentionEngagementTeamLegalOfficerUploadAddendumEvidence(
                templateId,
                personalisationProvider,
                detEmailService,
                documentDownloadClient
        );

        initializePrefixesForInternalAppeal(detentionEngagementTeamLegalOfficerUploadAddendumEvidence);

        Map<String, String> appellantInfo = new HashMap<>();
        appellantInfo.put("appellantGivenNames", appellantGivenNames);
        appellantInfo.put("appellantFamilyName", appellantFamilyName);
        appellantInfo.put("homeOfficeReferenceNumber", homeOfficeReferenceNumber);
        appellantInfo.put("appealReferenceNumber", appealReferenceNumber);

        when(personalisationProvider.getAppellantPersonalisation(asylumCase)).thenReturn(appellantInfo);

        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(uploadAdditionalEvidenceDocId)));

        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));

        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(jsonDocument);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(
                templateId,
                detentionEngagementTeamLegalOfficerUploadAddendumEvidence.getTemplateId(asylumCase)
        );
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + uploadAdditionalEvidencePersonalisationReferenceId,
                detentionEngagementTeamLegalOfficerUploadAddendumEvidence.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detEmailService.getRecipientsList(asylumCase)).thenReturn(Collections.singleton(detEmailAddress));

        assertTrue(
                detentionEngagementTeamLegalOfficerUploadAddendumEvidence.getRecipientsList(asylumCase).contains(detEmailAddress));
    }

    @Test
    void getRecipientsList_should_return_empty_set_if_not_in_detention() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertEquals(Collections.emptySet(), detentionEngagementTeamLegalOfficerUploadAddendumEvidence.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_no_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());
        assertEquals(Collections.emptySet(), detentionEngagementTeamLegalOfficerUploadAddendumEvidence.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_other_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        assertEquals(Collections.emptySet(), detentionEngagementTeamLegalOfficerUploadAddendumEvidence.getRecipientsList(asylumCase));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
                () -> detentionEngagementTeamLegalOfficerUploadAddendumEvidence.getPersonalisationForLink((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_legal_officer_upload_addendum_evidence_letter_is_missing() {
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> detentionEngagementTeamLegalOfficerUploadAddendumEvidence.getPersonalisationForLink(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("legalOfficerUploadAdditionalEvidenceLetter document not available");
    }

    @ParameterizedTest
    @EnumSource(YesOrNo.class)
    public void should_return_personalisation_when_all_information_given(YesOrNo yesOrNo) {

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.ofNullable(yesOrNo));

        Map<String, Object> personalisationForLink = detentionEngagementTeamLegalOfficerUploadAddendumEvidence.getPersonalisationForLink(asylumCase);

        assertEquals(appellantGivenNames, personalisationForLink.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisationForLink.get("appellantFamilyName"));
        assertEquals(appealReferenceNumber, personalisationForLink.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, personalisationForLink.get("homeOfficeReferenceNumber"));
        assertEquals(jsonDocument, personalisationForLink.get("documentLink"));

        if (yesOrNo.equals(YES)) {
            assertEquals(adaPrefix, personalisationForLink.get("subjectPrefix"));
        } else {
            assertEquals(nonAdaPrefix, personalisationForLink.get("subjectPrefix"));
        }
    }

    @Test
    public void should_throw_exception_when_notification_client_throws_Exception() throws NotificationClientException, IOException {
        when(documentDownloadClient.getJsonObjectFromDocument(uploadAdditionalEvidenceDoc)).thenThrow(new NotificationClientException("File size is more than 2MB"));
        assertThatThrownBy(() -> detentionEngagementTeamLegalOfficerUploadAddendumEvidence.getPersonalisationForLink(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to get Legal Officer upload addendum evidence letter in compatible format");
    }


}
