package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixesForInternalAppeal;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.TestUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionEngagementTeamDecideAnApplicationPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private MakeAnApplicationService makeAnApplicationService;
    @Mock
    private MakeAnApplication makeAnApplication;
    @Mock
    private DetEmailService detEmailService;

    private final Long caseId = 12345L;
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String detentionEngagementTeamDecideAnApplicationApplicantTemplateId = "detentionEngagementTeamDecideAnApplicationApplicantTemplateId";
    private final JSONObject jsonObject = new JSONObject("{\"title\": \"JsonDocument\"}");
    DocumentWithMetadata decideAnApplicationLetter = TestUtils.getDocumentWithMetadata(
            "id", "internal_appeal_submission", "some other desc", DocumentTag.INTERNAL_DECIDE_AN_APPELLANT_APPLICATION_LETTER);
    IdValue<DocumentWithMetadata> document = new IdValue<>("1", decideAnApplicationLetter);
    private final String nonAdaPrefix = "IAFT - SERVE IN PERSON";
    private final String adaPrefix = "ADA - SERVE IN PERSON";
    @Mock
    DocumentDownloadClient documentDownloadClient;

    private DetentionEngagementTeamDecideAnApplicationPersonalisation detentionEngagementTeamDecideAnApplicationPersonalisation;

    @BeforeEach
    void setup() throws NotificationClientException, IOException {
        detentionEngagementTeamDecideAnApplicationPersonalisation = new DetentionEngagementTeamDecideAnApplicationPersonalisation(
            detentionEngagementTeamDecideAnApplicationApplicantTemplateId,
            customerServicesProvider,
            makeAnApplicationService,
            detEmailService,
            documentDownloadClient
        );
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(document)));
        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(jsonObject);
        when((makeAnApplicationService.getMakeAnApplication(asylumCase, true))).thenReturn(Optional.of(makeAnApplication));

    }

    @Test
    void should_return_given_template_id() {
        assertEquals(detentionEngagementTeamDecideAnApplicationApplicantTemplateId,
            detentionEngagementTeamDecideAnApplicationPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_DECIDE_AN_APPLICATION_DET",
            detentionEngagementTeamDecideAnApplicationPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_det_email_address() {
        String detentionEngagementTeamEmail = "det@email.com";
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detEmailService.getRecipientsList(asylumCase)).thenReturn(Collections.singleton(detentionEngagementTeamEmail));

        assertTrue(
            detentionEngagementTeamDecideAnApplicationPersonalisation.getRecipientsList(asylumCase).contains(detentionEngagementTeamEmail));
    }

    @Test
    void getRecipientsList_should_return_empty_set_if_not_in_detention() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertEquals(Collections.emptySet(), detentionEngagementTeamDecideAnApplicationPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_no_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());
        assertEquals(Collections.emptySet(), detentionEngagementTeamDecideAnApplicationPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_other_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        assertEquals(Collections.emptySet(), detentionEngagementTeamDecideAnApplicationPersonalisation.getRecipientsList(asylumCase));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnPersonalisationForRefused(boolean isAcceleratedDetained) {
        when(makeAnApplication.getDecision()).thenReturn("Refused");
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAcceleratedDetained ? YES : NO));
        initializePrefixesForInternalAppeal(detentionEngagementTeamDecideAnApplicationPersonalisation);
        Map<String, Object> personalisationForLink = detentionEngagementTeamDecideAnApplicationPersonalisation.getPersonalisationForLink(asylumCase);
        //assert the personalisation map values
        assertEquals(isAcceleratedDetained ? adaPrefix : nonAdaPrefix, personalisationForLink.get("subjectPrefix"));
        assertEquals(appellantGivenNames, personalisationForLink.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisationForLink.get("appellantFamilyName"));
        assertEquals(appealReferenceNumber, personalisationForLink.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, personalisationForLink.get("homeOfficeReferenceNumber"));
        assertEquals(jsonObject, personalisationForLink.get("documentLink"));
        assertEquals(isAcceleratedDetained ? "*IAFT-ADA4: Make an application – Accelerated detained appeal (ADA)" : "*IAFT-DE4: Make an application – Detained appeal",
                personalisationForLink.get("form"));
        assertEquals(isAcceleratedDetained ? "https://www.gov.uk/government/publications/make-an-application-accelerated-detained-appeal-form-iaft-ada4" : "https://www.gov.uk/government/publications/make-an-application-detained-appeal-form-iaft-de4",
                personalisationForLink.get("formLink"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnPersonalisationForGranted(boolean isAcceleratedDetained) {
        when(makeAnApplication.getDecision()).thenReturn("Granted");
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAcceleratedDetained ? YES : NO));
        initializePrefixesForInternalAppeal(detentionEngagementTeamDecideAnApplicationPersonalisation);
        Map<String, Object> personalisationForLink = detentionEngagementTeamDecideAnApplicationPersonalisation.getPersonalisationForLink(asylumCase);
        //assert the personalisation map values
        assertEquals(isAcceleratedDetained ? adaPrefix : nonAdaPrefix, personalisationForLink.get("subjectPrefix"));
        assertEquals(appellantGivenNames, personalisationForLink.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisationForLink.get("appellantFamilyName"));
        assertEquals(appealReferenceNumber, personalisationForLink.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, personalisationForLink.get("homeOfficeReferenceNumber"));
        assertEquals(jsonObject, personalisationForLink.get("documentLink"));
        assertEquals("", personalisationForLink.get("form"));
        assertEquals("", personalisationForLink.get("formLink"));
    }
    
    @Test
    void should_throw_exception_when_make_an_application_list_is_empty() {
        when((makeAnApplicationService.getMakeAnApplication(asylumCase, true))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> detentionEngagementTeamDecideAnApplicationPersonalisation.getPersonalisationForLink(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("MakeAnApplication is not present");
    }
}
