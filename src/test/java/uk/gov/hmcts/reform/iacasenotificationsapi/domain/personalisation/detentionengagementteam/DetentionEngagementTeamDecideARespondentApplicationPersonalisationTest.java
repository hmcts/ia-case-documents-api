package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixesForInternalAppealByPost;

import java.io.IOException;
import java.util.Collections;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.TestUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DetentionEngagementTeamDecideARespondentApplicationPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private DetEmailService detEmailService;
    @Mock
    DocumentDownloadClient documentDownloadClient;

    private final Long caseId = 12345L;
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";

    private final JSONObject jsonObject = new JSONObject("{\"title\": \"JsonDocument\"}");

    DocumentWithMetadata decideAnApplicationLetter = TestUtils.getDocumentWithMetadata(
            "id", "respondent_application_decided", "some other desc", DocumentTag.INTERNAL_DECIDE_HOME_OFFICE_APPLICATION_LETTER);
    IdValue<DocumentWithMetadata> document = new IdValue<>("1", decideAnApplicationLetter);

    private final String nonAdaPrefix = "IAFT - SERVE BY POST";
    private final String adaPrefix = "ADA - SERVE BY POST";

    private final String detentionEngagementTeamDecideAnApplicationApplicantTemplateId = "Some template id";

    private DetentionEngagementTeamDecideARespondentApplicationPersonalisation detentionEngagementTeamDecideARespondentApplicationPersonalisation;

    @BeforeEach
    void setup() throws NotificationClientException, IOException {
        detentionEngagementTeamDecideARespondentApplicationPersonalisation = new DetentionEngagementTeamDecideARespondentApplicationPersonalisation(
                detentionEngagementTeamDecideAnApplicationApplicantTemplateId,
                customerServicesProvider,
                detEmailService,
                documentDownloadClient
        );
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(document)));

        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(jsonObject);

        initializePrefixesForInternalAppealByPost(detentionEngagementTeamDecideARespondentApplicationPersonalisation);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(detentionEngagementTeamDecideAnApplicationApplicantTemplateId,
                detentionEngagementTeamDecideARespondentApplicationPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_DECIDE_A_RESPONDENT_APPLICATION_DET",
                detentionEngagementTeamDecideARespondentApplicationPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_det_email_address() {
        String detentionEngagementTeamEmail = "det@email.com";
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detEmailService.getRecipientsList(asylumCase)).thenReturn(Collections.singleton(detentionEngagementTeamEmail));

        assertTrue(
                detentionEngagementTeamDecideARespondentApplicationPersonalisation.getRecipientsList(asylumCase).contains(detentionEngagementTeamEmail));
    }

    @Test
    void getRecipientsList_should_return_empty_set_if_not_in_detention() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertEquals(Collections.emptySet(), detentionEngagementTeamDecideARespondentApplicationPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_no_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());
        assertEquals(Collections.emptySet(), detentionEngagementTeamDecideARespondentApplicationPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_other_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        assertEquals(Collections.emptySet(), detentionEngagementTeamDecideARespondentApplicationPersonalisation.getRecipientsList(asylumCase));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class)
    void shouldReturnPersonalisationForRefused(YesOrNo yesOrNo) {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(yesOrNo));

        Map<String, Object> personalisationForLink = detentionEngagementTeamDecideARespondentApplicationPersonalisation.getPersonalisationForLink(asylumCase);

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
    public void should_throw_exception_on_personalisation_when_respondent_application_decided_document_is_missing() {
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(
                        () -> detentionEngagementTeamDecideARespondentApplicationPersonalisation.getPersonalisationForLink(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("internalDecideHomeOfficeApplicationLetter document not available");
    }

}
