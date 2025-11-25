package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.caseofficer.editdocument;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CASE_NOTES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.EmailAddressFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CaseOfficerEditDocumentsPersonalisationTest {

    private final String beforeListingTemplateId = "beforeListingTemplateId";
    private final String afterListingTemplateId = "afterListingTemplateId";

    @Mock
    private EmailAddressFinder emailAddressFinder;
    @Mock
    private EditDocumentService editDocumentService;
    @Mock
    private AppealService appealService;
    @Mock
    private AsylumCase asylumCase;
    private CaseOfficerEditDocumentsPersonalisation personalisation;
    @Mock
    FeatureToggler featureToggler;
    @Captor
    private ArgumentCaptor<List<String>> argCaptor;

    private static Object[] generateDifferentCaseNotesScenarios() {
        String multiLineReason = "line 1 reason" + System.lineSeparator() + "line 2 reason";
        String singleLine = "line 1 reason";
        return new Object[] {
            new Object[] {generateSingleCaseNoteWithMultiLineReason(), multiLineReason},
            new Object[] {generateSingleCaseNoteWithSingleLineReason(), singleLine},
            new Object[] {generateTwoCaseNotesWithMultiLineReasons(), multiLineReason},
        };
    }

    private static Callback<AsylumCase> generateTwoCaseNotesWithMultiLineReasons() {
        String multiLineReason = "line 1 reason" + System.lineSeparator() + "line 2 reason";
        IdValue<CaseNote> idCaseNote1 = buildCaseNote(multiLineReason);

        String singleLine = "line 1 reason";
        IdValue<CaseNote> idCaseNote2 = buildCaseNote(singleLine);
        AsylumCase asylumCase = new AsylumCase();
        writeCaseNote(asylumCase, Arrays.asList(idCaseNote1, idCaseNote2));
        return buildTestCallback(asylumCase);
    }

    private static Callback<AsylumCase> generateSingleCaseNoteWithMultiLineReason() {
        String multiLineReason = "line 1 reason" + System.lineSeparator() + "line 2 reason";
        IdValue<CaseNote> idCaseNote = buildCaseNote(multiLineReason);
        AsylumCase asylumCase = new AsylumCase();
        writeCaseNote(asylumCase, Collections.singletonList(idCaseNote));
        return buildTestCallback(asylumCase);
    }

    private static Callback<AsylumCase> generateSingleCaseNoteWithSingleLineReason() {
        String singleLine = "line 1 reason";
        IdValue<CaseNote> idCaseNote = buildCaseNote(singleLine);
        AsylumCase asylumCase = new AsylumCase();
        writeCaseNote(asylumCase, Collections.singletonList(idCaseNote));
        return buildTestCallback(asylumCase);
    }

    private static Callback<AsylumCase> buildTestCallback(AsylumCase asylumCase) {
        CaseDetails<AsylumCase> caseDetails = new CaseDetails<>(1L, "IA", State.APPEAL_SUBMITTED,
            asylumCase, LocalDateTime.now());
        CaseDetails<AsylumCase> caseDetailsBefore = new CaseDetails<>(1L, "IA", State.APPEAL_SUBMITTED,
            new AsylumCase(), LocalDateTime.now());
        return new Callback<>(caseDetails, Optional.of(caseDetailsBefore), Event.EDIT_DOCUMENTS);
    }

    private static void writeCaseNote(AsylumCase asylumCase, List<IdValue<CaseNote>> caseNoteList) {
        asylumCase.write(CASE_NOTES, caseNoteList);
        asylumCase.write(APPEAL_REFERENCE_NUMBER, "RP/50001/2020");
        asylumCase.write(APPELLANT_GIVEN_NAMES, "Lacy Dawson");
        asylumCase.write(APPELLANT_FAMILY_NAME, "Venus Blevins");
    }

    private static IdValue<CaseNote> buildCaseNote(String reason) {
        CaseNote caseNote = new CaseNote("subject", buildCaseNoteDescription(reason), "user",
            LocalDate.now().toString());
        return new IdValue<>("1", caseNote);
    }

    private static String buildCaseNoteDescription(String reason) {
        return String.format("Document names: %s" + System.lineSeparator() + "reason: %s" + System.lineSeparator(),
            Arrays.asList("some doc name", "some other doc name"), reason);
    }

    @BeforeEach
    public void setUp() {
        personalisation = new CaseOfficerEditDocumentsPersonalisation(
            beforeListingTemplateId,
            afterListingTemplateId,
            emailAddressFinder,
            editDocumentService,
            "http://localhost",
            appealService, featureToggler);
    }

    @Test
    public void getReferenceId() {
        assertEquals("1234_APPEAL_DOCUMENT_DELETED_CASE_OFFICER", personalisation.getReferenceId(1234L));
    }

    @Test
    public void getTemplateId() {
        when(appealService.isAppealListed(asylumCase)).thenReturn(false);
        assertEquals(beforeListingTemplateId, personalisation.getTemplateId(asylumCase));

        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        assertEquals(afterListingTemplateId, personalisation.getTemplateId(asylumCase));
    }

    @Test
    public void getRecipientsList_when_feature_flag_is_Off() {
        assertTrue(
                personalisation.getRecipientsList(asylumCase).isEmpty());
    }

    @Test
    public void getRecipientsList_when_feature_flag_is_On() {
        when(featureToggler.getValue("tcw-notifications-feature", false)).thenReturn(true);
        given(emailAddressFinder.getHearingCentreEmailAddress(any(AsylumCase.class)))
                .willReturn("hearingCentre@email.com");

        assertTrue(personalisation.getRecipientsList(new AsylumCase()).contains("hearingCentre@email.com"));
    }

    @ParameterizedTest
    @MethodSource("generateDifferentCaseNotesScenarios")
    public void getPersonalisation(Callback<AsylumCase> callback, String expectedReason) {
        initializePrefixes(personalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        FormattedDocument formattedDocument = new FormattedDocument("some file name", "some desc");
        given(editDocumentService.getFormattedDocumentsGivenCaseAndDocNames(any(), any()))
            .willReturn(new FormattedDocumentList(Collections.singletonList(formattedDocument)));

        Map<String, String> actualPersonalisation = personalisation.getPersonalisation(callback);

        assertEquals("RP/50001/2020", actualPersonalisation.get("appealReferenceNumber"));
        assertEquals("Lacy Dawson", actualPersonalisation.get("appellantGivenNames"));
        assertEquals("Venus Blevins", actualPersonalisation.get("appellantFamilyName"));
        assertEquals("http://localhost", actualPersonalisation.get("linkToOnlineService"));
        assertEquals(expectedReason, actualPersonalisation.get("reasonForEditingOrDeletingDocuments"));

        then(editDocumentService).should(times(1))
            .getFormattedDocumentsGivenCaseAndDocNames(any(AsylumCase.class), argCaptor.capture());

        List<String> actualDocNames = argCaptor.getValue();
        assertThat(actualDocNames).containsOnly("some doc name", "some other doc name");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(() -> personalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }
}
