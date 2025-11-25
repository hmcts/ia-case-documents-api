package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixesForInternalAppealByPost;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.TestUtils;
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
public class DetentionEngagementTeamRequestResponseAmendPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    private DetEmailService detEmailService;
    @Mock
    DocumentDownloadClient documentDownloadClient;

    private final String detentionEngagementTeamRequestResponseAmendTemplateId = "Some template id";
    private final String referenceId = "_REQUEST_RESPONSE_AMEND_DET";
    private final Long caseId = 12345L;
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final JSONObject jsonObject = new JSONObject("{\"title\": \"JsonDocument\"}");
    DocumentWithMetadata requestResponseAmendLetter = TestUtils.getDocumentWithMetadata(
            "id", "home-office-amend-appeal-response", "some other desc", DocumentTag.AMEND_HOME_OFFICE_APPEAL_RESPONSE);
    IdValue<DocumentWithMetadata> document = new IdValue<>("1", requestResponseAmendLetter);
    private final String nonAdaPrefix = "IAFT - SERVE BY POST";
    private final String adaPrefix = "ADA - SERVE BY POST";
    private final String detentionEngagementTeamEmail = "det@email.com";
    private DetentionEngagementTeamRequestResponseAmendPersonalisation detentionEngagementTeamRequestResponseAmendPersonalisation;

    @BeforeEach
    void setup() throws NotificationClientException, IOException {
        detentionEngagementTeamRequestResponseAmendPersonalisation = new DetentionEngagementTeamRequestResponseAmendPersonalisation(
                detentionEngagementTeamRequestResponseAmendTemplateId,
                personalisationProvider,
                detEmailService,
                documentDownloadClient
        );

        Map<String, String> appelantInfo = new HashMap<>();
        appelantInfo.put("appellantGivenNames", appellantGivenNames);
        appelantInfo.put("appellantFamilyName", appellantFamilyName);
        appelantInfo.put("homeOfficeReferenceNumber", homeOfficeReferenceNumber);
        appelantInfo.put("appealReferenceNumber", appealReferenceNumber);

        when(personalisationProvider.getAppellantPersonalisation(asylumCase)).thenReturn(appelantInfo);

        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(document)));

        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));

        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(jsonObject);

        initializePrefixesForInternalAppealByPost(detentionEngagementTeamRequestResponseAmendPersonalisation);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(detentionEngagementTeamRequestResponseAmendTemplateId,
                detentionEngagementTeamRequestResponseAmendPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + referenceId,
                detentionEngagementTeamRequestResponseAmendPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_det_email_address() {

        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detEmailService.getRecipientsList(asylumCase)).thenReturn(Collections.singleton(detentionEngagementTeamEmail));

        assertTrue(detentionEngagementTeamRequestResponseAmendPersonalisation
                .getRecipientsList(asylumCase)
                .contains(detentionEngagementTeamEmail));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_no_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());
        assertEquals(Collections.emptySet(), detentionEngagementTeamRequestResponseAmendPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_other_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        assertEquals(Collections.emptySet(), detentionEngagementTeamRequestResponseAmendPersonalisation.getRecipientsList(asylumCase));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class)
    void should_return_personalisation_when_all_information_given(YesOrNo yesOrNo) {

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.ofNullable(yesOrNo));

        Map<String, Object> personalisationForLink = detentionEngagementTeamRequestResponseAmendPersonalisation.getPersonalisationForLink(asylumCase);

        assertEquals(appellantGivenNames, personalisationForLink.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisationForLink.get("appellantFamilyName"));
        assertEquals(appealReferenceNumber, personalisationForLink.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, personalisationForLink.get("homeOfficeReferenceNumber"));
        assertEquals(jsonObject, personalisationForLink.get("documentLink"));

        if (yesOrNo.equals(YES)) {
            assertEquals(adaPrefix, personalisationForLink.get("subjectPrefix"));
        } else {
            assertEquals(nonAdaPrefix, personalisationForLink.get("subjectPrefix"));
        }
    }

    @Test
    public void should_throw_exception_on_personalisation_when_amend_home_office_response_letter_is_missing() {
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(
                        () -> detentionEngagementTeamRequestResponseAmendPersonalisation.getPersonalisationForLink(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("amendHomeOfficeAppealResponse document not available");
    }

}
