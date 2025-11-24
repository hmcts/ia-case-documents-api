package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.TestUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.PrisonNomsNumber;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetentionFacilityEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;
import uk.gov.service.notify.NotificationClientException;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionEngagementTeamListCaseProductionPersonalisationTest {
    @Mock
    AsylumCase asylumCase;
    @Mock
    private DateTimeExtractor dateTimeExtractor;
    @Mock
    private DetentionFacilityEmailService detentionFacilityEmailService;
    @Mock
    private HearingDetailsFinder hearingDetailsFinder;
    @Mock
    JSONObject jsonDocument;

    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String someBuilding = "someBuilding";
    private final String hearingDate = "25-12-2025";
    private final String hearingTime = "12:00:00";
    private final String hearingCentreAddress = "someAddress";
    private final String templateId = "templateId";
    private final String nonAdaPrefix = "IAFT - SERVE IN PERSON";
    private DetentionEngagementTeamListCaseProductionPersonalisation personalisation;

    DocumentWithMetadata caseListedDoc = TestUtils.getDocumentWithMetadata(
            "id", "detained-appellant-list-case-letter", "some other desc", DocumentTag.INTERNAL_LIST_CASE_LETTER);
    IdValue<DocumentWithMetadata> caseListedBundle = new IdValue<>("1", caseListedDoc);

    @BeforeEach
    public void setup() throws NotificationClientException, IOException {
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(caseListedBundle)));

        personalisation = new DetentionEngagementTeamListCaseProductionPersonalisation(
            templateId,
                detentionFacilityEmailService,
            dateTimeExtractor,
            hearingDetailsFinder,
            nonAdaPrefix
        );
    }

    @Test
    public void should_return_given_template_id_detained() {
        assertEquals(
            templateId,
            personalisation.getTemplateId()
        );
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_DETAINED_CASE_LISTED_PRODUCTION_DET", personalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_det_email_address() {
        String detentionEngagementTeamEmail = "det@email.com";
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detentionFacilityEmailService.getDetentionEmailAddress(asylumCase)).thenReturn(detentionEngagementTeamEmail);

        assertTrue(personalisation.getRecipientsList(asylumCase).contains(detentionEngagementTeamEmail));
    }

    @Test
    public void should_return_personalisation_when_all_information_given_for_prison() {
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(DETENTION_BUILDING, String.class)).thenReturn(Optional.of(someBuilding));
        when(dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getHearingDateTime(asylumCase))).thenReturn(hearingDate);
        when(dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getHearingDateTime(asylumCase))).thenReturn(hearingTime);
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCase)).thenReturn(hearingCentreAddress);
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));
        PrisonNomsNumber prisonNomsNumber = mock(PrisonNomsNumber.class);
        when(asylumCase.read(PRISON_NOMS, PrisonNomsNumber.class)).thenReturn(Optional.of(prisonNomsNumber));
        String prisonNoms = "12345";
        when(prisonNomsNumber.getPrison()).thenReturn(prisonNoms);

        Map<String, String> actualPersonalisation = personalisation.getPersonalisation(asylumCase);

        assertEquals(nonAdaPrefix, actualPersonalisation.get("subjectPrefix"));
        assertEquals(appealReferenceNumber, actualPersonalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, actualPersonalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, actualPersonalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, actualPersonalisation.get("appellantFamilyName"));
        assertEquals(someBuilding, actualPersonalisation.get("detentionBuilding"));
        assertEquals(hearingDate, actualPersonalisation.get("hearingDate"));
        assertEquals(hearingTime, actualPersonalisation.get("hearingTime"));
        assertEquals(hearingCentreAddress, actualPersonalisation.get("hearingCentreAddress"));
        assertEquals("NOMS Ref: 12345", actualPersonalisation.get("nomsRef"));
    }

    @Test
    public void should_return_personalisation_when_all_information_given_for_irc() {
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(DETENTION_BUILDING, String.class)).thenReturn(Optional.of("someBuilding"));
        when(dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getHearingDateTime(asylumCase))).thenReturn("25-12-2025");
        when(dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getHearingDateTime(asylumCase))).thenReturn("12:00:00");
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCase)).thenReturn("someAddress");
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));

        Map<String, String> actualPersonalisation = personalisation.getPersonalisation(asylumCase);

        assertEquals(nonAdaPrefix, actualPersonalisation.get("subjectPrefix"));
        assertEquals(appealReferenceNumber, actualPersonalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, actualPersonalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, actualPersonalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, actualPersonalisation.get("appellantFamilyName"));
        assertEquals(someBuilding, actualPersonalisation.get("detentionBuilding"));
        assertEquals(hearingDate, actualPersonalisation.get("hearingDate"));
        assertEquals(hearingTime, actualPersonalisation.get("hearingTime"));
        assertEquals(hearingCentreAddress, actualPersonalisation.get("hearingCentreAddress"));
        assertEquals("", actualPersonalisation.get("nomsRef"));
    }

    @Test
    public void should_return_personalisation_when_all_information_is_missing() {
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(DETENTION_BUILDING, String.class)).thenReturn(Optional.empty());
        when(dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getHearingDateTime(asylumCase))).thenReturn("25-12-2025");
        when(dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getHearingDateTime(asylumCase))).thenReturn("12:00:00");
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCase)).thenReturn("someAddress");

        Map<String, String> actualPersonalisation = personalisation.getPersonalisation(asylumCase);

        assertEquals(nonAdaPrefix, actualPersonalisation.get("subjectPrefix"));
        assertEquals("", actualPersonalisation.get("appealReferenceNumber"));
        assertEquals("", actualPersonalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", actualPersonalisation.get("appellantGivenNames"));
        assertEquals("", actualPersonalisation.get("appellantFamilyName"));
        assertEquals("", actualPersonalisation.get("detentionBuilding"));
        assertEquals(hearingDate, actualPersonalisation.get("hearingDate"));
        assertEquals(hearingTime, actualPersonalisation.get("hearingTime"));
        assertEquals(hearingCentreAddress, actualPersonalisation.get("hearingCentreAddress"));
    }
}
