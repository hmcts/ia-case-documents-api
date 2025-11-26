package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.PrisonNomsNumber;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DetentionFacilityEmailService;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DETENTION_BUILDING;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.PRISON_NOMS;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionEngagementTeamHearingCancelledProductionPersonalisationTest {

    private static final String TEMPLATE_ID = "templateId";
    private static final String EMAIL = "detention@email.com";
    private static final String SUBJECT_PREFIX = "someSubject";
    private static final String PRISON = "prison";
    private static final String OTHER = "other";

    private DetentionEngagementTeamHearingCancelledProductionPersonalisation personalisation;

    @Mock
    private DetentionFacilityEmailService detentionFacilityEmailService;
    @Mock
    private DateTimeExtractor dateTimeExtractor;
    @Mock
    private HearingDetailsFinder hearingDetailsFinder;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock
    private AsylumCase asylumCaseBefore;
    @Mock
    private PrisonNomsNumber prisonNomsNumber;

    @BeforeEach
    void setUp() {
        personalisation = new DetentionEngagementTeamHearingCancelledProductionPersonalisation(
                TEMPLATE_ID, detentionFacilityEmailService, dateTimeExtractor, hearingDetailsFinder, SUBJECT_PREFIX);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_return_template_id() {
        assertThat(personalisation.getTemplateId()).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void should_return_reference_id() {
        assertThat(personalisation.getReferenceId(12345L))
                .isEqualTo("12345_DETAINED_HEARING_CANCELLED_PRODUCTION_DET");
    }

    @Test
    void should_return_recipients_if_detained_in_prison() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(PRISON));
        when(detentionFacilityEmailService.getDetentionEmailAddress(asylumCase)).thenReturn(EMAIL);

        Set<String> recipients = personalisation.getRecipientsList(asylumCase);
        assertThat(recipients).containsExactly(EMAIL);
    }

    @Test
    void should_return_personalisation_with_values() {
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(hearingDetailsFinder.getHearingDateTime(asylumCaseBefore)).thenReturn("2024-06-01T10:00");
        when(dateTimeExtractor.extractHearingDate("2024-06-01T10:00")).thenReturn("01-06-2024");
        when(dateTimeExtractor.extractHearingTime("2024-06-01T10:00")).thenReturn("10:00");
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCaseBefore)).thenReturn("some address");

        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(PRISON));
        when(asylumCase.read(PRISON_NOMS, PrisonNomsNumber.class)).thenReturn(Optional.of(prisonNomsNumber));
        when(prisonNomsNumber.getPrison()).thenReturn("ABC123");

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("REF123"));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("HO123"));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("John"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Doe"));
        when(asylumCase.read(DETENTION_BUILDING, String.class)).thenReturn(Optional.of("Building X"));

        Map<String, String> personalisationMap = personalisation.getPersonalisation(callback);

        assertThat(personalisationMap)
                .containsEntry("subjectPrefix", SUBJECT_PREFIX)
                .containsEntry("appealReferenceNumber", "REF123")
                .containsEntry("homeOfficeReferenceNumber", "HO123")
                .containsEntry("appellantGivenNames", "John")
                .containsEntry("appellantFamilyName", "Doe")
                .containsEntry("nomsRef", "NOMS Ref: ABC123")
                .containsEntry("hearingDate", "01-06-2024")
                .containsEntry("hearingTime", "10:00")
                .containsEntry("hearingCentreAddress", "some address")
                .containsEntry("detentionBuilding", "Building X");
    }

    @Test
    void should_return_empty_hearing_info_if_no_previous_case_details() {
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.empty());
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(PRISON));
        when(asylumCase.read(PRISON_NOMS, PrisonNomsNumber.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(DETENTION_BUILDING, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisationMap = personalisation.getPersonalisation(callback);

        assertThat(personalisationMap.get("hearingDate")).isEmpty();
        assertThat(personalisationMap.get("hearingTime")).isEmpty();
        assertThat(personalisationMap.get("hearingCentreAddress")).isEmpty();
    }

    @Test
    void should_throw_exception_on_null_callback() {
        Callback<AsylumCase> callback = null;
        assertThrows(NullPointerException.class, () -> personalisation.getPersonalisation(callback));
    }
}
